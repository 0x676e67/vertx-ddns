package com.zf1976.ddns.verticle;

import com.zf1976.ddns.pojo.DNSAccount;
import com.zf1976.ddns.util.JSONUtil;
import com.zf1976.ddns.util.ObjectUtil;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.util.Validator;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/6
 */
public class ApiVerticle extends RouterVerticle {

    private final Logger log = LogManager.getLogger(ApiVerticle.class);

    public ApiVerticle() {

    }

    @Override
    public void start(Promise<Void> startPromise) {
        final var router = getRouter();
        final var httpServer = vertx.createHttpServer()
                                    .exceptionHandler(Throwable::printStackTrace);
        // 存储DNS服务商密钥
        router.post("/api/storeAccount")
              .consumes("application/json")
              .handler(this::storeAccount);

        httpServer.requestHandler(router)
                  .listen(configProperty.getServerPort())
                  .onSuccess(event -> {
                     log.info("Vertx web server initialized with port(s): " + configProperty.getServerPort() + " (http)");
                     startPromise.complete();
                  })
                  .onFailure(startPromise::fail);

    }

    private void storeAccount(RoutingContext routingContext) {
        try {
            final var dnsAccountDTO = routingContext.getBodyAsJson()
                                                    .mapTo(DNSAccount.class);
            switch (dnsAccountDTO.getDnsServiceType()) {
                case ALIYUN:
                case HUAWEI:
                case DNSPOD:
                    Validator.of(dnsAccountDTO)
                             .withValidated(v -> !StringUtil.isEmpty(v.getId()) && !v.getId()
                                                                                     .isBlank(),
                                     () -> new RuntimeException("ID cannot be empty"))
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret()
                                                                                         .isBlank(),
                                     () -> new RuntimeException("The Secret cannot be empty"));
                    break;
                case CLOUDFLARE:
                    Validator.of(dnsAccountDTO)
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()),
                                     () -> new RuntimeException("The Secret cannot be empty"));
                default:
            }
            this.storeAccountAndHandle(routingContext, dnsAccountDTO)
                .onSuccess(success -> this.returnJsonWithCache(routingContext))
                .onFailure(err -> this.handleError(routingContext, err));
        } catch (RuntimeException e) {
            routingContext.fail(400, e);
        } catch (Exception e) {
            routingContext.fail(400, new RuntimeException("Parameter abnormal"));
        }
    }

    private Future<Void> storeAccountAndHandle(RoutingContext routingContext, DNSAccount dnsAccount) {
        final var fileSystem = vertx.fileSystem();
        final String configFilePath = workDir + "/account.json";
        return fileSystem.exists(configFilePath)
                         .compose(v -> {
                             // 配置文件不存在,则创建
                             if (!v) {
                                 return fileSystem.createFile(configFilePath)
                                                  .compose(create -> fileSystem.readFile(configFilePath));
                             }
                             return fileSystem.readFile(configFilePath);
                         })
                         .compose(buffer -> this.accountHandler(buffer, dnsAccount, configFilePath));
    }

    private Future<Void> accountHandler(Buffer buffer, DNSAccount dnsAccount, String configFilePath) {
        // 数据为空
        if (ObjectUtil.isEmpty(buffer.getBytes())) {
            List<DNSAccount> accountList = new ArrayList<>();
            accountList.add(dnsAccount);
            return this.writeFile(configFilePath, JSONUtil.toJsonString(accountList));
        } else {
            try {
                List<DNSAccount> ddnsAccountList = new ArrayList<>();
                final var list = JSONUtil.readValue(buffer.toString(), List.class);
                if (list != null) {
                    for (Object obj : list) {
                        final var decodeDnsAccount = JsonObject.mapFrom(obj).mapTo(DNSAccount.class);
                        ddnsAccountList.add(decodeDnsAccount);
                    }
                    ddnsAccountList.add(dnsAccount);
                    return this.writeFile(configFilePath, JSONUtil.toJsonString(ddnsAccountList));
                } else {
                    // 手动修改配置文件后 可能会读取错误
                    return Future.failedFuture(new RuntimeException("File read error configuration"));
                }
            } catch (Exception e) {
                return Future.failedFuture(new RuntimeException("Server Error"));
            }
        }
    }

    public Future<Void> writeFile(String configFilePath, String json) {
        return vertx.fileSystem()
                    .writeFile(configFilePath, Buffer.buffer(json));
    }


}
