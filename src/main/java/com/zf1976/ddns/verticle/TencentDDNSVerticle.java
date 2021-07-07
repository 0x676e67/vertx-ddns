package com.zf1976.ddns.verticle;

import io.vertx.core.Promise;

/**
 * @author mac
 * @date 2021/7/6
 */
public class TencentDDNSVerticle extends RouterVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final var router = super.getRouter();
        final var httpServer = vertx.createHttpServer();
        router.get("/tencent")
              .handler(request -> {
                  final var response = request.response();
                  response.end("hello tencent");
              });
        httpServer.requestHandler(router).listen(8080)
                  .onSuccess(event -> {
                      System.out.println("腾讯云DDNS服务启动...");
                  })
                  .onFailure(System.out::println);
        startPromise.complete();
    }
}
