package com.zf1976.ddns.verticle;

import com.zf1976.ddns.pojo.DNSAccountDTO;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.util.Validator;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;

/**
 * @author mac
 * @date 2021/7/6
 */
public class ApiVerticle extends RouterVerticle {

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
                  .listen(8080)
                  .onSuccess(event -> {
                      System.out.println("DDNS服务启动...");
                      startPromise.complete();
                  })
                  .onFailure(startPromise::fail);

    }

    private void storeAccount(RoutingContext routingContext) {
        try {
            final var dnsAccountDTO = routingContext.getBodyAsJson()
                                                    .mapTo(DNSAccountDTO.class);
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
        } catch (RuntimeException e) {
            routingContext.fail(400, e);
        } catch (Exception e) {
            routingContext.fail(400, new RuntimeException("Parameter abnormal"));
        }
    }

    private void storeAndHandle(RoutingContext routingContext, DNSAccountDTO dnsAccountDTO) {
        final var fileSystem = vertx.fileSystem();

        this.returnJsonWithCache(routingContext);
    }
}
