package com.zf1976.ddns.verticle;

import com.zf1976.ddns.pojo.DNSAccount;
import com.zf1976.ddns.util.JSONUtil;
import com.zf1976.ddns.util.ObjectUtil;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.util.Validator;
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
            this.storeAccountAndHandle(routingContext, dnsAccountDTO);
        } catch (RuntimeException e) {
            routingContext.fail(400, e);
        } catch (Exception e) {
            routingContext.fail(400, new RuntimeException("Parameter abnormal"));
        }
    }

    private void storeAccountAndHandle(RoutingContext routingContext, DNSAccount dnsAccount) {
        final var fileSystem = vertx.fileSystem();
        final String configFilePath = workDir + "/account.json";
        fileSystem.exists(configFilePath)
                  .compose(v -> {
                      // 配置文件不存在,则创建
                      if (!v) {
                          return fileSystem.createFile(configFilePath)
                                           .compose(create -> fileSystem.readFile(configFilePath));
                      }
                      return fileSystem.readFile(configFilePath);
                  })
                  .onComplete(v -> {
                      if (v.succeeded()) {
                          // 数据为空
                          if (ObjectUtil.isEmpty(v.result().getBytes())) {
                              List<DNSAccount> accountList = new ArrayList<>();
                              accountList.add(dnsAccount);
                              final var json = JSONUtil.toJsonString(accountList);
                              vertx.fileSystem()
                                   .writeFile(configFilePath, Buffer.buffer(json))
                                   .onFailure(err -> {
                                       log.error(err.getMessage(), err.getCause());
                                       routingContext.fail(500);
                                   })
                                   .onSuccess(success -> {
                                       this.returnJsonWithCache(routingContext);
                                   });
                          } else {
                              try {
                                  List<DNSAccount> ddnsAccountList = new ArrayList<>();
                                  final var list = JSONUtil.readValue(v.result().toString(), List.class);
                                  if (list != null) {
                                      for (Object obj : list) {
                                          final var decodeDnsAccount = JsonObject.mapFrom(obj).mapTo(DNSAccount.class);
                                          ddnsAccountList.add(decodeDnsAccount);
                                      }
                                      ddnsAccountList.add(dnsAccount);
                                      fileSystem.writeFile(configFilePath, Buffer.buffer(JSONUtil.toJsonString(ddnsAccountList)))
                                                .onSuccess(success -> {
                                                    this.returnJsonWithCache(routingContext);
                                                })
                                                .onFailure(err -> {
                                                    log.error(err.getMessage(), err.getCause());
                                                    routingContext.fail(500);
                                                });
                                  }
                              } catch (Exception e) {
                                  log.error(e.getMessage(), e.getCause());
                                  routingContext.fail(500);
                              }

                          }
                      } else {
                          log.error(v.cause());
                          routingContext.fail(500);
                      }
                  });
    }

    private void storeAccountConfig(DNSAccount dnsAccountDTO, String path) {

    }
}
