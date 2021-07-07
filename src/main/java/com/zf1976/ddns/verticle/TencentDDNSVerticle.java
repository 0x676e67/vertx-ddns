package com.zf1976.ddns.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * @author mac
 * @date 2021/7/6
 */
public class TencentDDNSVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        startPromise.complete();
    }
}
