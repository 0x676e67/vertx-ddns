package com.zf1976.ddns.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

/**
 * @author mac
 * @date 2021/7/7
 */
public abstract class RouterVerticle extends AbstractVerticle {

    private volatile static Router router;

    protected synchronized Router getRouter() {
        return router;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        if (router == null) {
            synchronized (RouterVerticle.class) {
                if (router == null) {
                    router = Router.router(vertx);
                    this.handleTemplate(router, vertx);
                }
            }
        }
        super.init(vertx, context);
    }

    private void handleTemplate(Router router, Vertx vertx) {
        TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);
        TemplateHandler handler = TemplateHandler.create(engine);
        router.get("/")
              .handler(handler);
        // 将所有以 `.html` 结尾的 GET 请求路由到模板处理器上
        router.getWithRegex(".+\\.html")
              .handler(handler);
        // 错误处理
        router.route("/*")
              .failureHandler(ErrorHandler.create(vertx));
        // 静态资源处理
        router.route("/*")
              .handler(StaticHandler.create());

    }
}
