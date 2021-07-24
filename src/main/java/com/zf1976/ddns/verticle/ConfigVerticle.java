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

    private final Logger log = LogManager.getLogger("[ConfigVerticle]");
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.init()
            .compose(json -> Future.<Void>succeededFuture()
                                   .compose(v -> vertx.deployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(json))))
            .onSuccess(event -> {
                startPromise.complete();
            }).onFailure(err -> {
                err.printStackTrace();
                log.error("Class：" + err.getClass() + " => Message：" + err.getMessage());
            vertx.close();
            });
    }

    private Future<JsonObject> init() {
        final Object load;
        try {
            load = ConfigProperty.getInstance()
                                 .getJsonConfig();
            final var jsonObject = JsonObject.mapFrom(load);
            // 获取端口号
            final var serverPort = config().getString(ApiConstants.SERVER_PORT);
            jsonObject.put(ApiConstants.SERVER_PORT, serverPort);
            return Future.succeededFuture(jsonObject);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
