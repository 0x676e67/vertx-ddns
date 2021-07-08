package com.zf1976.ddns;

import com.zf1976.ddns.verticle.MainVerticle;
import io.vertx.core.Vertx;

/**
 * @author mac
 * @date 2021/7/6
 */
public class DDNSApplication {

    public static void main(String[] args) {
        // 将日志实现强制设为 Log4j 2
        Vertx.vertx().deployVerticle(new MainVerticle());
    }

}
