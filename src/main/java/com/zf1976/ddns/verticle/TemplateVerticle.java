package com.zf1976.ddns.verticle;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.pojo.DDNSConfig;
import com.zf1976.ddns.pojo.DataResult;
import com.zf1976.ddns.util.IpUtil;
import com.zf1976.ddns.util.JSONUtil;
import com.zf1976.ddns.util.RsaUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/7
 */
public abstract class TemplateVerticle extends AbstractVerticle {

    private final Logger log = LogManager.getLogger(TemplateVerticle.class);
    private volatile static Router router;
    protected static String workDir = null;
    protected static final String WORK_DIR_NAME = ".ddns";
    protected static final String DDNS_CONFIG_FILENAME = "ddns_config.json";
    protected static final String ACCOUNT_FILENAME = "account.json";
    protected static final String RSA_KEY_FILENAME = "rsa_key.json";
    protected RsaUtil.RsaKeyPair rsaKeyPair;

    protected synchronized Router getRouter() {
        return router;
    }

    protected Integer serverPort() {
        return Integer.valueOf(config().getString(ApiConstants.SERVER_PORT));
    }

    @Override
    public void init(Vertx vertx, Context context) {
        if (router == null) {
            synchronized (TemplateVerticle.class) {
                if (router == null) {
                    router = Router.router(vertx);
                    this.initProjectConfig(vertx);
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
     protected void initProjectConfig(Vertx vertx) {
        final var fileSystem = vertx.fileSystem();
        final var projectWorkPath = this.pathToAbsolutePath(System.getProperty("user.home"), WORK_DIR_NAME);
        final var ddnsConfigFilePath = this.pathToAbsolutePath(projectWorkPath, DDNS_CONFIG_FILENAME);
        final var accountFilePath = this.pathToAbsolutePath(projectWorkPath, ACCOUNT_FILENAME);
        final var rsaKeyPath = this.pathToAbsolutePath(projectWorkPath, RSA_KEY_FILENAME);
        fileSystem.mkdirs(projectWorkPath)
                  .compose(v -> fileSystem.exists(ddnsConfigFilePath))
                  .compose(bool -> createFile(fileSystem, bool, ddnsConfigFilePath))
                  .compose(v -> fileSystem.exists(accountFilePath))
                  .compose(bool -> createFile(fileSystem, bool, accountFilePath))
                  .compose(v ->fileSystem.exists(rsaKeyPath))
                  .compose(bool -> createFile(fileSystem, bool, rsaKeyPath))
                  .compose(v -> {
                      try {
                          final var rsaKeyPair = RsaUtil.generateKeyPair();
                          this.rsaKeyPair = rsaKeyPair;
                          return fileSystem.writeFile(rsaKeyPath, Buffer.buffer(Json.encodePrettily(rsaKeyPair)));
                      } catch (NoSuchAlgorithmException e) {
                          return Future.failedFuture(e);
                      }
                  })
                  .onSuccess(succeed -> {
                      log.info("Initialize project working directory：" + projectWorkPath);
                      log.info("Initialize DDNS configuration file：" + ddnsConfigFilePath);
                      log.info("Initialize account configuration file：" + accountFilePath);
                      log.info("Initialize rsa key configuration file：" + rsaKeyPath);
                      log.info("RSA key has been initialized");
                      TemplateVerticle.workDir = projectWorkPath;
                      this.handleTemplate(router, vertx);
                  })
                  .onFailure(err -> {
                      log.error(err.getMessage(), err.getCause());
                      System.exit(0);
                  });
    }

    private Future<Void> createFile(FileSystem fileSystem, boolean bool, String path) {
        if (!bool) {
            return fileSystem.createFile(path);
        }
        return Future.succeededFuture();
    }

    protected Future<RsaUtil.RsaKeyPair> readRsaKeyPair() {
        if (this.rsaKeyPair != null) {
            return Future.succeededFuture(this.rsaKeyPair);
        }
        return vertx.fileSystem()
                    .readFile(pathToAbsolutePath(workDir, RSA_KEY_FILENAME))
                    .compose(v -> Future.succeededFuture(JSONUtil.readValue(v.toString(), RsaUtil.RsaKeyPair.class)));
    }

    @SuppressWarnings("rawtypes")
    protected Future<List> getDDNSConfig(Vertx vertx) {
        return vertx.fileSystem()
                    .readFile(pathToAbsolutePath(workDir, DDNS_CONFIG_FILENAME))
                    .compose(v -> Future.succeededFuture(JSONUtil.readValue(v.toString(), List.class)));
    }

    protected String pathToAbsolutePath(String first,String ...more) {
        return Paths.get(first,more)
                    .toFile()
                    .getAbsolutePath();
    }

    private Future<DDNSConfig> ddnsConfigDecryptHandler(DDNSConfig ddnsConfig) {
        return this.readRsaKeyPair()
                   .compose(buffer -> {
                       final var rsaKeyPair = JSONUtil.readValue(buffer.toString(), RsaUtil.RsaKeyPair.class);
                       if (rsaKeyPair == null) {
                           return Future.failedFuture("RSA keyless");
                       }
                       try {
                           ddnsConfig.setId(RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), ddnsConfig.getId()));
                           ddnsConfig.setSecret(RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), ddnsConfig.getSecret()));
                           return Future.succeededFuture(ddnsConfig);
                       } catch (Exception e) {
                           return Future.failedFuture(e.getMessage());
                       }
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
                  getDDNSConfig(vertx).compose(ddnsList -> this.readRsaKeyPair()
                                                             .onSuccess(keyPair -> {
                                                                 ctx.put("common", ConfigProperty.getCommonProperties());
                                                                 ctx.put("ipv4", IpUtil.getNetworkIpv4List());
                                                                 ctx.put("ipv6", IpUtil.getNetworkIpv6List());
                                                                 ctx.put("ddnsConfigList", ddnsList);
                                                                 ctx.put("rsaPublicKey", keyPair.getPublicKey());
                                                                 handler.handle(ctx);
                                                             })).onFailure(err -> ctx.response()
                                                                                     .setStatusCode(500)
                                                                                     .end(Json.encodePrettily(DataResult.fail(500, "Failed to read configuration file"))));
              });
        // 静态资源处理
        router.get().handler(StaticHandler.create());

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
