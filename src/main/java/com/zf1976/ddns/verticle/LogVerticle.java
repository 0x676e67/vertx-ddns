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

import com.zf1976.ddns.util.StringUtil;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * @author mac
 * 2021/9/5 Sunday 5:11 PM
 */
public class LogVerticle extends AbstractWebServerVerticle {

    private final Logger log = LogManager.getLogger("[LogVerticle]");

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final var router = super.getRouter();
        final var fileSystem = vertx.fileSystem();
        final var relativePath = Paths.get("").toAbsolutePath().toString();
        final var absolutePath = super.toAbsolutePath(relativePath, LOGS_FILENAME);
        router.get("/api/log/list")
                .handler(ctx -> {
                    fileSystem.readDir(absolutePath)
                            .onSuccess(event -> {
                                final var fileNameList = event.stream()
                                        .map(v -> v.replace(absolutePath + "/", ""))
                                        .collect(Collectors.toList());
                                super.routeSuccessHandler(ctx, Json.encodePrettily(fileNameList));
                            })
                            .onFailure(err -> super.routeErrorHandler(ctx, err));
                });
        router.post("/api/log")
                .handler(ctx -> {
                    final var request = ctx.request();
                    final var filename = request.getParam("filename");
                    if (StringUtil.isEmpty(filename)) {
                        this.routeBadRequestHandler(ctx, "filename cannot been null!");
                        return;
                    }
                    fileSystem.readFile(this.toAbsolutePath(absolutePath, filename))
                            .onSuccess(buffer -> {
                                ctx.response().send(buffer);
                            })
                            .onFailure(err -> this.routeErrorHandler(ctx, err.getMessage()));
                });
        router.delete("/api/log")
                .handler(ctx -> {
                    final var request = ctx.request();
                    final var filename = request.getParam("filename");
                    if (StringUtil.isEmpty(filename)) {
                        this.routeBadRequestHandler(ctx, "filename cannot been null!");
                        return;
                    }
                    fileSystem.delete(this.toAbsolutePath(absolutePath, filename))
                            .onSuccess(v -> this.routeSuccessHandler(ctx))
                            .onFailure(err -> this.routeErrorHandler(ctx, err.getMessage()));
                });

        super.start(startPromise);
    }
}
