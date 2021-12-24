/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zf1976.ddns.verticle;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.ObjectUtil;
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
public class DeployVerticle extends AbstractVerticle {

    private final Logger log = LogManager.getLogger("[DeployVerticle]");
    private final String[] args;

    public DeployVerticle(String[] args) {
        this.args = args;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.init()
            .compose(json -> {
                final var deploymentOptions = new DeploymentOptions().setConfig(json);
                HttpUtil.initCustomWebClient(vertx);
                return Future.<Void>succeededFuture()
                        .compose(v -> vertx.deployVerticle(new WebServerVerticle(), deploymentOptions))
                        .compose(v -> vertx.deployVerticle(new LogVerticle(),deploymentOptions));
            })
            .onSuccess(event -> {
                startPromise.complete();
            })
            .onFailure(err -> {
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
            // set service port configuration
            if (ObjectUtil.isEmpty(this.args)) {
                jsonObject.put(ApiConstants.SERVER_PORT, 8080);
            } else {
                jsonObject.put(ApiConstants.SERVER_PORT, args[0]);
            }
            return Future.succeededFuture(jsonObject);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Future.failedFuture(e);
        }
    }

}
