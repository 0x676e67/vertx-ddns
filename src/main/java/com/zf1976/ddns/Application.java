package com.zf1976.ddns;

import com.zf1976.ddns.util.ObjectUtil;
import com.zf1976.ddns.verticle.ApiConstants;
import com.zf1976.ddns.verticle.ConfigVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author mac
 * @date 2021/7/6
 */
public class Application {

    public static void main(String[] args) {
        final var deploymentOptions = new DeploymentOptions();
        if (!ObjectUtil.isEmpty(args)) {
            deploymentOptions.setConfig(new JsonObject().put(ApiConstants.SERVER_PORT, args[0]));
        } else {
            deploymentOptions.setConfig(new JsonObject().put(ApiConstants.SERVER_PORT, 8080));
        }
        Vertx.vertx().deployVerticle(new ConfigVerticle(), deploymentOptions);
    }

}
