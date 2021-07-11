package com.zf1976.ddns.verticle;

import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.pojo.DDNSConfig;
import com.zf1976.ddns.service.AliyunDDNSService;
import com.zf1976.ddns.util.JSONUtil;
import com.zf1976.ddns.util.ObjectUtil;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.util.Validator;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
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
    private final AliyunDDNSService aliyunDDNSService;
    public ApiVerticle() {
        this.aliyunDDNSService = new AliyunDDNSService(ConfigProperty.getAliyunDnsProperties());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        final var serverPort = serverPort();
        final var router = getRouter();
        final var httpServer = vertx.createHttpServer().exceptionHandler(Throwable::printStackTrace);
        // 存储DNS服务商密钥
        router.post("/api/storeConfig").handler(this::storeDDNSConfigHandle);
        // 查询DNS服务商域名解析记录
        router.post("/api/ddnsRecord").handler(this::findDDNSRecordsHandler);
        // 删除解析记录
        router.delete("/api/ddnsRecord").handler(this::deleteDDNSRecordHandler);
        // 获取RSA公钥
        router.get("/api/rsa/publicKey").handler(ctx -> this.readRsaKeyPair()
                                                        .onSuccess(rsaKeyPair -> this.returnJsonWithCache(ctx, rsaKeyPair.getPublicKey())));
        // 异常处理
        router.route("/api/*").failureHandler(this::returnError);
        httpServer.requestHandler(router)
                  .listen(serverPort)
                  .onSuccess(event -> {
                     log.info("Vertx web server initialized with port(s): " + serverPort + " (http)");
                     startPromise.complete();
                  })
                  .onFailure(startPromise::fail);
    }

    protected void findDDNSRecordsHandler(RoutingContext routingContext) {
        final var request = routingContext.request();
        final var param = request.getParam(ApiConstants.DDNS_SERVICE_TYPE);
        final var type = DDNSServiceType.checkType(param);
        try {
            if (ObjectUtil.isEmpty(type)) {
                throw new RuntimeException("The DDNS service provider does not exist");
            }
            final var domain = request.getParam(ApiConstants.DOMAIN);
            switch (type) {
                case ALIYUN:
                    DescribeDomainRecordsResponse describeDomainRecordsResponse;
                    if (domain != null) {
                        describeDomainRecordsResponse = this.aliyunDDNSService.findDescribeDomainRecords(domain);
                    } else {
                        describeDomainRecordsResponse = this.aliyunDDNSService.findDescribeDomainRecords();
                    }
                    super.returnJsonWithCache(routingContext, describeDomainRecordsResponse.getDomainRecords());
                    break;
                case CLOUDFLARE:
                case HUAWEI:
                case DNSPOD:
                default:
            }
        } catch (RuntimeException exception) {
            this.handleBad(routingContext, exception);
        } catch (Exception exception) {
            this.handleError(routingContext, new RuntimeException("Parameter abnormal"));
        }
    }

    protected void deleteDDNSRecordHandler(RoutingContext routingContext) {
        try {
            final var recordId = routingContext.request().getParam(ApiConstants.RECORD_ID);
            this.aliyunDDNSService.deleteDomainRecordResponse(recordId);
            this.returnJsonWithCache(routingContext);
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
                             .withValidated(v -> !StringUtil.isEmpty(v.getId()) && !v.getId()
                                                                                     .isBlank(),
                                     () -> new RuntimeException("ID cannot be empty"))
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret()
                                                                                         .isBlank(),
                                     () -> new RuntimeException("The Secret cannot be empty"));
                    break;
                case CLOUDFLARE:
                    Validator.of(ddnsConfig)
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()),
                                     () -> new RuntimeException("The Secret cannot be empty"));
                default:
            }
            this.storeDDNSConfig(ddnsConfig)
                .onSuccess(success -> this.returnJsonWithCache(routingContext))
                .onFailure(err -> this.handleError(routingContext, err));
        } catch (RuntimeException exception) {
            this.handleBad(routingContext, exception);
        } catch (Exception exception) {
            this.handleBad(routingContext, new RuntimeException("Parameter abnormal"));
        }
    }

    private Future<Void> storeDDNSConfig(DDNSConfig ddnsConfig) {
        final var fileSystem = vertx.fileSystem();
        final String configFilePath = this.pathToAbsolutePath(workDir, DDNS_CONFIG_FILENAME);
        return fileSystem.exists(configFilePath)
                         .compose(v -> {
                             // 配置文件不存在,则创建
                             if (!v) {
                                 return fileSystem.createFile(configFilePath)
                                                  .compose(create -> fileSystem.readFile(configFilePath));
                             }
                             return fileSystem.readFile(configFilePath);
                         })
                         .compose(buffer -> this.ddnsConfigWriteHandle(buffer, ddnsConfig, configFilePath));
    }

    private Future<Void> ddnsConfigWriteHandle(Buffer buffer, DDNSConfig ddnsConfig, String configFilePath) {
        // 数据为空
        if (ObjectUtil.isEmpty(buffer.getBytes())) {
            List<DDNSConfig> accountList = new ArrayList<>();
            accountList.add(ddnsConfig);
            return this.writeConfig(configFilePath, JSONUtil.toJsonString(accountList));
        } else {
            try {
                List<DDNSConfig> configArrayList = new ArrayList<>();
                final var rawConfigList = JSONUtil.readValue(buffer.toString(), List.class);
                if (rawConfigList != null) {
                    for (Object obj : rawConfigList) {
                        final var decodeDnsConfig = JSONUtil.readValue(obj, DDNSConfig.class);
                        configArrayList.add(decodeDnsConfig);
                    }
                    configArrayList.removeIf(config -> config.getDnsServiceType().equals(ddnsConfig.getDnsServiceType()));
                    configArrayList.add(ddnsConfig);
                    return this.writeConfig(configFilePath, JSONUtil.toJsonString(configArrayList));
                } else {
                    // 手动修改配置文件后 可能会读取错误
                    return Future.failedFuture(new RuntimeException("File read error configuration"));
                }
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
