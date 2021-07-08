package com.zf1976.ddns.verticle;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.property.CommonProperties;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

/**
 * @author mac
 * @date 2021/7/7
 */
public abstract class RouterVerticle extends AbstractVerticle {

    private final Logger log = LogManager.getLogger(RouterVerticle.class);
    private volatile static Router router;
    protected static CommonProperties configProperty = ConfigProperty.getCommonProperties();

    protected synchronized Router getRouter() {
        return router;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        if (router == null) {
            synchronized (RouterVerticle.class) {
                if (router == null) {
                    router = Router.router(vertx);
                    this.initProjectDir(vertx);
                    this.handleTemplate(router, vertx);
                }
            }
        }
        super.init(vertx, context);
    }

    /**
     * 初始化项目工作目录
     *
     * @param vertx vertx
     */
    private void initProjectDir(Vertx vertx) {
        final var fileSystem = vertx.fileSystem();
        final var path = Paths.get(System.getProperty("user.home"), ".ddns");
        final var absolutePath = path.toFile().getAbsolutePath();
        fileSystem.exists(absolutePath)
                  .onSuccess(bool -> {
                      // 目录不存在则创建
                      if (!bool) {
                          fileSystem.mkdirs(absolutePath)
                                    .onSuccess(v -> {
                                        log.info("Create project working directory：" + absolutePath);
                                    })
                                    .onFailure(err -> {
                                        log.error(err.getMessage(), err.getCause());
                                        System.exit(0);
                                    });
                      }
                  })
                  .onFailure(err -> {
                     log.error(err.getMessage(), err.getCause());
                     System.exit(0);
                  });
    }

    private void handleTemplate(Router router, Vertx vertx) {
        TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);
        TemplateHandler handler = TemplateHandler.create(engine);
        // 设置默认模版
        handler.setIndexTemplate("index.html");
        // Body处理
        router.route().handler(BodyHandler.create());
        // 将所有以 `.html` 结尾的 GET 请求路由到模板处理器上
        router.getWithRegex(".+\\.html")
              .handler(handler);
        // 路径定义错误处理器
        router.route("/api/*").failureHandler(this::returnError);

    }

    private void returnError(RoutingContext routingContext) {
        JsonObject result = new JsonObject();
        int errorCode = routingContext.statusCode() > 0 ? routingContext.statusCode() : 500;
        // 不懂 Vert.x 为什么 EventBus 和 Web 是两套异常系统
        if (routingContext.failure() instanceof ReplyException) {
            errorCode = ((ReplyException) routingContext.failure()).failureCode();
        }
        result.put("errorCode", errorCode);
        if (routingContext.failure() != null) {
            result.put("reason", routingContext.failure().getMessage());
        }
        setCommonHeader(routingContext.response()
                                      .setStatusCode(errorCode)
                                      .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8"))
                .end(result.encodePrettily());
    }

    private HttpServerResponse setCommonHeader(HttpServerResponse response) {
        return response
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Cache-Control", "no-cache");
    }

    protected void returnJsonWithCache(RoutingContext routingContext, JsonObject jsonObject) {
        routingContext.response()
                      .putHeader("content-type", "application/json; charset=utf-8")
                      .end(jsonObject.encodePrettily());
    }

    protected void returnJsonWithCache(RoutingContext routingContext) {
        this.returnJsonWithCache(routingContext, new JsonObject());
    }
}
