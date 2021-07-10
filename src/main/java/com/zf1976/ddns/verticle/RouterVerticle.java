package com.zf1976.ddns.verticle;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.pojo.DataResult;
import com.zf1976.ddns.property.CommonProperties;
import com.zf1976.ddns.util.IpUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
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
    protected static String workDir = null;
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
     protected void initProjectDir(Vertx vertx) {
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
                      RouterVerticle.workDir = absolutePath;
                  })
                  .onFailure(err -> {
                     log.error(err.getMessage(), err.getCause());
                     System.exit(0);
                  });
    }

    private void handleTemplate(Router router, Vertx vertx) {
        TemplateEngine templateEngine = ThymeleafTemplateEngine.create(vertx);
        TemplateHandler handler = TemplateHandler.create(templateEngine);
        // 设置默认模版
        handler.setIndexTemplate("index.html");
        // 将所有以 `.html` 结尾的 GET 请求路由到模板处理器上
        router.getWithRegex(".+\\.html")
              .handler(ctx -> {
                  ctx.put("common",ConfigProperty.getCommonProperties());
                  ctx.put("ipv4", IpUtil.getNetworkIpv4List());
                  ctx.put("ipv6", IpUtil.getNetworkIpv6List());
                  ctx.put("ddnsConfig","");
                  handler.handle(ctx);
              });
        // 静态资源处理
        router.get().handler(StaticHandler.create());
        // 路径定义错误处理器/设置Content-Type
        router.route()
              .consumes("application/json")
              .handler(BodyHandler.create());

    }

    protected void returnError(RoutingContext routingContext) {
        int errorCode = routingContext.statusCode() > 0 ? routingContext.statusCode() : 500;
        // 不懂 Vert.x 为什么 EventBus 和 Web 是两套异常系统
        if (routingContext.failure() instanceof ReplyException) {
            errorCode = ((ReplyException) routingContext.failure()).failureCode();
        }
        final var result = DataResult.fail(errorCode, routingContext.failure().getMessage());
        setCommonHeader(routingContext.response()
                                      .setStatusCode(errorCode)
                                      .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8"))
                .end(Json.encodePrettily(result));
    }

    private HttpServerResponse setCommonHeader(HttpServerResponse response) {
        return response
                .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    }

    protected void returnJsonWithCache(RoutingContext routingContext, Object object) {
        routingContext.response()
                      .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                      .end(Json.encodePrettily(DataResult.success(object)));
    }

    protected void returnJsonWithCache(RoutingContext routingContext) {
        this.returnJsonWithCache(routingContext, null);
    }

    protected void handleError(RoutingContext routingContext, Throwable throwable) {
        this.handleException(routingContext, 500, throwable);
    }

    protected void handleBad(RoutingContext routingContext, Throwable throwable) {
        this.handleException(routingContext, 400, throwable);
    }

    protected void handleException(RoutingContext routingContext, int statusCode, Throwable throwable) {
        log.error(throwable.getMessage(), throwable.getCause());
        routingContext.fail(statusCode, throwable);
    }
}
