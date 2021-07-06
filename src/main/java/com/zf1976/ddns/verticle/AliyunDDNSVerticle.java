package com.zf1976.ddns.verticle;

import com.zf1976.ddns.annotation.YamlPrefix;
import com.zf1976.ddns.property.AliyunDnsProperties;
import com.zf1976.ddns.util.PropertyUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

/**
 * @author mac
 * @date 2021/7/6
 */
public class AliyunDDNSVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        System.out.println(config());
        final var properties = PropertyUtil.getProperties(AliyunDnsProperties.class, config());
        if (properties != null) {

        }
        startPromise.complete();
    }
}
