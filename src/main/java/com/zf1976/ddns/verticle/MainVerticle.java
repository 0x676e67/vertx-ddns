package com.zf1976.ddns.verticle;

import com.zf1976.ddns.annotation.YamlPrefix;
import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.property.AliyunDnsProperties;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.PropertyUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * @author mac
 * @date 2021/7/6
 */
public class MainVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger("[ConfigVerticle]");
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.initConfig()
            .compose(json -> {
                if (json != null) {
                    return Future.<Void>succeededFuture()
                                 .compose(v -> vertx.deployVerticle(new AliyunDDNSVerticle(), new DeploymentOptions().setConfig(json)))
                                 .compose(v -> vertx.deployVerticle(new TencentDDNSVerticle(), new DeploymentOptions().setConfig(json)));
                }
                return Future.failedFuture("json config is empty");
            })
            .onSuccess(event -> {
                startPromise.complete();
            }).onFailure(startPromise::fail);
    }

    private Future<JsonObject> initConfig() {
        final Object load;
        try {
            load = ConfigProperty.getInstance().getJsonConfig();
            return Future.succeededFuture(JsonObject.mapFrom(load));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
