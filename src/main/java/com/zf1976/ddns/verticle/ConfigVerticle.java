package com.zf1976.ddns.verticle;

import com.zf1976.ddns.config.ConfigProperty;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author mac
 * @date 2021/7/6
 */
public class ConfigVerticle extends AbstractVerticle {

    private final Logger log = LogManager.getLogger("[MainVerticle]");
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.init()
            .compose(json -> {
                if (json != null) {
                    return Future.<Void>succeededFuture()
                                 .compose(v -> vertx.deployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(json)));
                }
                return Future.failedFuture("json config is empty");
            })
            .onSuccess(event -> {
                startPromise.complete();
            }).onFailure(err -> {
                log.error("Class：" + err.getClass() + "=> Message：" + err.getMessage());
                log.info("ApiVerticle deploy failure");
                System.exit(0);
            });
    }

    private Future<JsonObject> init() {
        final Object load;
        try {
            load = ConfigProperty.getInstance().getJsonConfig();
            final var entries = JsonObject.mapFrom(load);
            // 获取端口号
            final var serverPort = config().getString(ApiConstants.SERVER_PORT);
            entries.put(ApiConstants.SERVER_PORT, serverPort);
            return Future.succeededFuture(entries);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
