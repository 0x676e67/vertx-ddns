package com.zf1976.ddns.verticle;

import com.zf1976.ddns.pojo.DNSAccountDTO;
import com.zf1976.ddns.util.Validator;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ErrorHandler;

/**
 * @author mac
 * @date 2021/7/6
 */
public class ApiVerticle extends RouterVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        final var router = getRouter();
        final var httpServer = vertx.createHttpServer().exceptionHandler(Throwable::printStackTrace);
        // 存储DNS服务商密钥
        router.post("/api/storeAccount")
              .consumes("application/json")
              .handler(this::storeAccount);

        router.route("/**").failureHandler(ErrorHandler.create(vertx));
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
            final var validated = Validator.of(dnsAccountDTO)
                                           .with(v -> v.getDnsServiceType() != null)
                                           .with(v -> v.getSecret() != null)
                                           .Validated();
            System.out.println(validated);
            routingContext.response().end();
        } catch (Exception e) {
            routingContext.fail(400, new RuntimeException("Parameter abnormal"));
        }
    }



    private void returnJsonWithCache(RoutingContext routingContext, JsonObject jsonObject) {
        routingContext.response()
                      .putHeader("content-type", "application/json; charset=utf-8")
                      .end(jsonObject.encodePrettily());
    }
}
