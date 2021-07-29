package com.zf1976.ddns.verticle;

import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.pojo.DDNSConfig;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.util.Validator;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/6
 */
public class ApiVerticle extends TemplateVerticle {

    private final Logger log = LogManager.getLogger(ApiVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        final var serverPort = serverPort();
        final var router = getRouter();
        final var httpServer = vertx.createHttpServer().exceptionHandler(Throwable::printStackTrace);
        // 存储DNS服务商密钥
        router.post("/api/storeConfig")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .handler(this::storeDDNSConfigHandle);
        // 查询DNS服务商域名解析记录
        router.post("/api/ddnsRecord").handler(this::findDDNSRecordsHandler);
        // 删除解析记录
        router.delete("/api/ddnsRecord").handler(this::deleteDDNSRecordHandler);
        // 获取RSA公钥
        router.get("/api/rsa/publicKey").handler(ctx -> this.readRsaKeyPair()
                                                            .onSuccess(rsaKeyPair -> this.returnJsonWithCache(ctx, rsaKeyPair.getPublicKey()))
                                                            .onFailure(err -> this.handleError(ctx, err)));
        // 异常处理
        router.route("/api/*").failureHandler(this::returnError);
        this.initConfig(vertx)
            .compose(v -> httpServer.requestHandler(router).listen(serverPort))
            .onSuccess(event -> {
                log.info("Vertx web server initialized with port(s): " + serverPort + " (http)");
                try {
                    super.start(startPromise);
                } catch (Exception e) {
                    log.error(e.getMessage(), e.getCause());
                    startPromise.fail(e);
                }
            })
            .onFailure(startPromise::fail);
    }

    @Override
    public void start() throws Exception {
//        vertx.setPeriodic(1000, id -> {
//            final var context = vertx.getOrCreateContext();
//            final var periodicId = context.get(ApiConstants.PERIODIC);
//            if (periodicId == null) {
//                context.put(ApiConstants.PERIODIC, id);
//            } else {
//                System.out.println(periodicId instanceof Long);
//                System.out.println(periodicId);
//            }
//        });
    }



    protected void findDDNSRecordsHandler(RoutingContext routingContext) {
        try {
            final var request = routingContext.request();
            final var ipRecordType = DNSRecordType.checkType(request.getParam(ApiConstants.IP_RECORD_TYPE));
            final var dnsServiceType = DNSServiceType.checkType(request.getParam(ApiConstants.DDNS_SERVICE_TYPE));
            final var domain = request.getParam(ApiConstants.DOMAIN);
            final var dataResult = this.dnsConfigTimerService.findDnsRecords(dnsServiceType, domain, ipRecordType);
            this.returnJsonWithCache(routingContext, dataResult);
        } catch (RuntimeException exception) {
            this.handleBad(routingContext, exception);
        } catch (Exception exception) {
            this.handleError(routingContext, new RuntimeException("Parameter abnormal"));
        }
    }

    protected void deleteDDNSRecordHandler(RoutingContext routingContext) {
        try {
            final var request = routingContext.request();
            final var recordId = request.getParam(ApiConstants.RECORD_ID);
            final var dnsServiceType = DNSServiceType.checkType(request.getParam(ApiConstants.DDNS_SERVICE_TYPE));
            final var domain = request.getParam(ApiConstants.DOMAIN);
            final var success = this.dnsConfigTimerService.deleteRecords(dnsServiceType, recordId, domain);
            this.returnJsonWithCache(routingContext, success);
        } catch (Exception e) {
            this.handleBad(routingContext, e);
        }
    }

    protected void storeDDNSConfigHandle(RoutingContext routingContext) {
        try {
            final var ddnsConfig = routingContext.getBodyAsJson().mapTo(DDNSConfig.class);
            switch (ddnsConfig.getDnsServiceType()) {
                case ALIYUN:
                case HUAWEI:
                case DNSPOD:
                    Validator.of(ddnsConfig)
                             .withValidated(v -> !StringUtil.isEmpty(v.getId()) && !v.getId().isBlank(), "ID cannot be empty")
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret().isBlank(), "The Secret cannot be empty");
                    break;
                case CLOUDFLARE:
                    Validator.of(ddnsConfig)
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret().isBlank(), "The Secret cannot be empty");
                default:
            }
            this.ddnsConfigDecryptHandler(ddnsConfig)
                .compose(this::storeDDNSConfig)
                .onSuccess(success -> this.returnJsonWithCache(routingContext))
                .onFailure(err -> this.handleError(routingContext, err));
        } catch (RuntimeException exception) {
            this.handleBad(routingContext, exception);
        } catch (Exception exception) {
            this.handleBad(routingContext, new RuntimeException("Parameter abnormal"));
        }
    }

    private Future<Void> storeDDNSConfig(DDNSConfig config) {
        final var fileSystem = vertx.fileSystem();
        final String configFilePath = this.pathToAbsolutePath(workDir, DDNS_CONFIG_FILENAME);
        return this.readDDNSConfig(fileSystem)
                   .compose(configList -> this.writeDDNSConfig(configList, config, configFilePath)
                                              .compose(v -> newDnsConfigTimerService(configList)));
    }

    private Future<Void> writeDDNSConfig(List<DDNSConfig> ddnsConfigs, DDNSConfig ddnsConfig, String configFilePath) {
        // 读取配置为空
        if (CollectionUtil.isEmpty(ddnsConfigs)) {
            List<DDNSConfig> accountList = new ArrayList<>();
            accountList.add(ddnsConfig);
            return this.writeConfig(configFilePath, Json.encodePrettily(accountList));
        } else {
            try {
                ddnsConfigs.removeIf(config -> ddnsConfig.getDnsServiceType().equals(config.getDnsServiceType()));
                ddnsConfigs.add(ddnsConfig);
                return this.writeConfig(configFilePath, Json.encodePrettily(ddnsConfigs));
            } catch (Exception e) {
                return Future.failedFuture(new RuntimeException("Server Error"));
            }
        }
    }

    private Future<Void> writeConfig(String configFilePath, String json) {
        return vertx.fileSystem()
                    .writeFile(configFilePath, Buffer.buffer(json));
    }


}
