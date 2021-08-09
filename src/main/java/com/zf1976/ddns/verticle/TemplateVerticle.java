package com.zf1976.ddns.verticle;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.pojo.DnsConfig;
import com.zf1976.ddns.pojo.DataResult;
import com.zf1976.ddns.pojo.SecureConfig;
import com.zf1976.ddns.util.*;
import com.zf1976.ddns.verticle.timer.DnsConfigTimerService;
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
import io.vertx.core.json.JsonObject;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/7
 */
public abstract class TemplateVerticle extends AbstractVerticle implements SecureProvider {

    private final Logger log = LogManager.getLogger("[TemplateVerticle]");
    private volatile static Router router;
    protected static String workDir = null;
    protected static final String WORK_DIR_NAME = ".ddns";
    protected static final String DDNS_CONFIG_FILENAME = "ddns_config.json";
    protected static final String SECURE_CONFIG_FILENAME = "secure_config.json";
    protected static final String RSA_KEY_FILENAME = "rsa_key.json";
    protected RsaUtil.RsaKeyPair rsaKeyPair;
    protected DnsConfigTimerService dnsConfigTimerService;
    protected Boolean notAllowWanAccess = Boolean.TRUE;


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
     protected Future<Void> initConfig(Vertx vertx) {
        final var fileSystem = vertx.fileSystem();
        final var projectWorkPath = this.toAbsolutePath(System.getProperty("user.home"), WORK_DIR_NAME);
        final var ddnsConfigFilePath = this.toAbsolutePath(projectWorkPath, DDNS_CONFIG_FILENAME);
        final var secureFilePath = this.toAbsolutePath(projectWorkPath, SECURE_CONFIG_FILENAME);
        final var rsaKeyPath = this.toAbsolutePath(projectWorkPath, RSA_KEY_FILENAME);
        return fileSystem.mkdirs(projectWorkPath)
                         .compose(v -> fileSystem.exists(ddnsConfigFilePath))
                         .compose(bool -> createFile(fileSystem, bool, ddnsConfigFilePath))
                         .compose(v -> fileSystem.exists(secureFilePath))
                         .compose(bool -> createFile(fileSystem, bool, secureFilePath))
                         .compose(v -> fileSystem.exists(rsaKeyPath))
                         .compose(bool -> createRsaKeyFile(fileSystem, bool, rsaKeyPath))
                         .compose(v -> {
                             log.info("Initialize project working directory：" + projectWorkPath);
                             log.info("Initialize DDNS configuration file：" + ddnsConfigFilePath);
                             log.info("Initialize secure configuration file：" + secureFilePath);
                             log.info("Initialize rsa key configuration file：" + rsaKeyPath);
                             log.info("RSA key has been initialized");
                             TemplateVerticle.workDir = projectWorkPath;
                             this.routeTemplateHandler(router, vertx);
                             return this.initDDNSServiceConfig(vertx.fileSystem());
                         });
     }

    private void routeTemplateHandler(Router router, Vertx vertx) {
        TemplateEngine templateEngine = ThymeleafTemplateEngine.create(vertx);
        TemplateHandler templateHandler = TemplateHandler.create(templateEngine);
        // 设置默认模版
        templateHandler.setIndexTemplate("index.html");
        // 将 "/"路径映射到 "/login.html"
        router.get("/")
              .handler(ctx -> ctx.redirect("/index.html"));
        // Mapping template
        router.getWithRegex(".+\\.html")
              .handler(ctx -> this.customTemplateHandler(ctx, templateHandler));
        // Static resource processing
        router.get("/*")
              .handler(StaticHandler.create());
    }

    protected void customTemplateHandler(RoutingContext ctx, TemplateHandler templateHandler) {
        readDDNSConfig(vertx.fileSystem())
                .compose(ddnsConfigList -> {
                    if (!CollectionUtil.isEmpty(ddnsConfigList)) {
                        for (DnsConfig ddnsConfig : ddnsConfigList) {
                            ddnsConfig.setId(this.hideHandler(ddnsConfig.getId()))
                                      .setSecret(this.hideHandler(ddnsConfig.getSecret()));
                        }
                    }
                    ctx.put("ddnsConfigList", ddnsConfigList);
                    return this.readRsaKeyPair();
                })
                .compose(rsaKeyPair -> {
                    if (rsaKeyPair != null) {
                        ctx.put("rsaPublicKey", rsaKeyPair.getPublicKey());
                    }
                    return this.readSecureConfig();
                })
                .onSuccess(secureConfig -> {
                    if (secureConfig != null) {
                        secureConfig.setPassword(this.hideHandler(secureConfig.getPassword()));
                        ctx.put("secureConfig", secureConfig);
                    }
                    ctx.put("common", ConfigProperty.getCommonProperties())
                       .put("ipv4", IpUtil.getNetworkIpv4List())
                       .put("ipv6", IpUtil.getNetworkIpv6List());
                    templateHandler.handle(ctx);
                })
                .onFailure(err -> this.routeErrorHandler(ctx, err));
    }

    protected Future<Void> initDDNSServiceConfig(FileSystem fileSystem) {
        return this.readDDNSConfig(fileSystem)
                   .compose(this::newDnsConfigTimerService);
    }

    protected Future<Void> newDnsConfigTimerService(List<DnsConfig> configList) {
        try {
            this.dnsConfigTimerService = new DnsConfigTimerService(configList, this.vertx);
            return Future.succeededFuture();
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return Future.failedFuture(e);
        }
    }

    private Future<Void> writeRsaKeyFile(FileSystem fileSystem, String rsaKeyPath) {
        try {
            final var rsaKeyPair = RsaUtil.generateKeyPair();
            this.rsaKeyPair = rsaKeyPair;
            return fileSystem.writeFile(rsaKeyPath, Buffer.buffer(Json.encodePrettily(rsaKeyPair)));
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e.getCause());
            return Future.failedFuture(e);
        }
    }

    private Future<Void> createRsaKeyFile(FileSystem fileSystem, boolean bool, String path) {
        if (!bool) {
            return fileSystem.createFile(path).compose(v -> writeRsaKeyFile(fileSystem, path));
        }
        return Future.succeededFuture();
    }

    private Future<Void> createFile(FileSystem fileSystem, boolean bool, String path) {
        if (!bool) {
            return fileSystem.createFile(path);
        }
        return Future.succeededFuture();
    }

    public Future<RsaUtil.RsaKeyPair> readRsaKeyPair() {
        if (this.rsaKeyPair != null) {
            return Future.succeededFuture(this.rsaKeyPair);
        }
        return vertx.fileSystem()
                    .readFile(toAbsolutePath(workDir, RSA_KEY_FILENAME))
                    .compose(buffer -> Future.succeededFuture(Json.decodeValue(buffer, RsaUtil.RsaKeyPair.class)));
    }

    public Future<SecureConfig> readSecureConfig() {
        String absolutePath = this.toAbsolutePath(workDir, SECURE_CONFIG_FILENAME);
        return vertx.fileSystem()
                    .readFile(absolutePath)
                    .compose(buffer -> {
                        try {
                            // config is empty
                            if (StringUtil.isEmpty(buffer.toString())) {
                                return Future.succeededFuture();
                            }
                            SecureConfig secureConfig = Json.decodeValue(buffer, SecureConfig.class);
                            this.notAllowWanAccess = secureConfig.getNotAllowWanAccess() == null? Boolean.TRUE : Boolean.FALSE;
                        return Future.succeededFuture(secureConfig);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e.getCause());
                        return Future.failedFuture(e);
                    }
                });
    }

    protected Future<List<DnsConfig>> readDDNSConfig(FileSystem fileSystem) {
        String absolutePath = toAbsolutePath(workDir, DDNS_CONFIG_FILENAME);
        return fileSystem.readFile(absolutePath)
                         .compose(buffer -> {
                             try {
                                 List<DnsConfig> configArrayList = new ArrayList<>();
                                 // config is empty
                                 if (StringUtil.isEmpty(buffer.toString())) {
                                     return Future.succeededFuture(configArrayList);
                                 }
                                 var list = Json.decodeValue(buffer, List.class);
                                 if (CollectionUtil.isEmpty(list)) {
                                     return Future.succeededFuture(configArrayList);
                                 }
                                 for (Object o : list) {
                                     configArrayList.add(JsonObject.mapFrom(o).mapTo(DnsConfig.class));
                                 }
                                 return Future.succeededFuture(configArrayList);
                             } catch (Exception e) {
                                 log.error(e.getMessage(), e.getCause());
                                 return Future.failedFuture(e);
                             }
                         });
    }

    protected String toAbsolutePath(String first, String ...more) {
        return Paths.get(first,more)
                    .toFile()
                    .getAbsolutePath();
    }

    protected Future<DnsConfig> ddnsConfigDecryptHandler(DnsConfig ddnsConfig) {
        return this.readRsaKeyPair()
                   .compose(keyPair -> this.ddnsConfigDecrypt(keyPair, ddnsConfig));
    }

    protected Future<SecureConfig> secureConfigDecryptHandler(SecureConfig secureConfig) {
        return this.readRsaKeyPair()
                .compose(rsaKeyPair -> {
                    if (rsaKeyPair == null) {
                        return Future.failedFuture("RSA keyless");
                    }
                    try {
                        secureConfig.setUsername(RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), secureConfig.getUsername()));
                        secureConfig.setPassword(RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), secureConfig.getPassword()));
                        return Future.succeededFuture(secureConfig);
                    } catch (Exception e) {
                        return this.readSecureConfig()
                                .compose(rawConfig -> {
                                    if (this.isHide(rawConfig.getPassword(), secureConfig.getPassword())) {
                                        secureConfig.setUsername(rawConfig.getUsername());
                                        secureConfig.setPassword(rawConfig.getPassword());
                                        return Future.succeededFuture(secureConfig);
                                    }
                                    return Future.failedFuture(e.getMessage());
                                });
                    }
                });
    }

    protected Future<DnsConfig> ddnsConfigDecrypt(RsaUtil.RsaKeyPair keyPair, DnsConfig ddnsConfig) {
        if (keyPair == null) {
            return Future.failedFuture("RSA keyless");
        }
        try {
            // cloudflare 只有token作为访问密钥
            if (!ddnsConfig.getDnsServiceType().equals(DnsServiceType.CLOUDFLARE)) {
                String id = RsaUtil.decryptByPrivateKey(keyPair.getPrivateKey(), ddnsConfig.getId());
                ddnsConfig.setId(id);
            }
            String secret = RsaUtil.decryptByPrivateKey(keyPair.getPrivateKey(), ddnsConfig.getSecret());
            ddnsConfig.setSecret(secret);
            return Future.succeededFuture(ddnsConfig);
        } catch (Exception e) {
            return readDDNSConfig(vertx.fileSystem())
                    .compose(ddnsConfigList -> {
                        for (DnsConfig rawConfig : ddnsConfigList) {
                            if (ddnsConfig.getDnsServiceType().equals(rawConfig.getDnsServiceType())) {
                                // cloudflare 只有token作为访问密钥
                                if (!ddnsConfig.getDnsServiceType().equals(DnsServiceType.CLOUDFLARE)) {
                                    if (this.isHide(rawConfig.getId(), ddnsConfig.getId()) && this.isHide(rawConfig.getSecret(), ddnsConfig.getSecret())) {
                                        ddnsConfig.setId(rawConfig.getId())
                                                .setSecret(rawConfig.getSecret());
                                        return Future.succeededFuture(ddnsConfig);
                                    }
                                } else {
                                    if (this.isHide(rawConfig.getSecret(), ddnsConfig.getSecret())) {
                                        return Future.succeededFuture(ddnsConfig.setSecret(rawConfig.getSecret()));
                                    }
                                }
                            }
                        }
                        return Future.failedFuture(e.getMessage());
                    });
        }
    }

    protected boolean isHide(String rawStr, String str) {
        return ObjectUtil.nullSafeEquals(hideHandler(rawStr), str);
    }

    protected String hideHandler(String rawStr) {
        if (StringUtil.isEmpty(rawStr)) {
            return "";
        }
        int beginHideIndex = 3;
        final var rawStrLength = rawStr.length();
        if ( rawStrLength > beginHideIndex) {
            final var noHide = rawStr.substring(0, beginHideIndex);
            final var beginHideStr = rawStr.substring(beginHideIndex);
            return noHide + "*".repeat(beginHideStr.length());
        }
        return "*".repeat(rawStrLength);
    }

    protected void routeErrorHandler(RoutingContext routingContext) {
        int errorCode = routingContext.statusCode() > 0 ? routingContext.statusCode() : 500;
        // 不懂 Vert.x 为什么 EventBus 和 Web 是两套异常系统
        if (routingContext.failure() instanceof ReplyException) {
            errorCode = ((ReplyException) routingContext.failure()).failureCode();
        }
        final var failure = routingContext.failure();
        final var result = DataResult.fail(errorCode, failure.getCause() != null? failure.getCause().getMessage() : failure.getMessage());
        try {
            this.setCommonHeader(routingContext.response()
                                               .setStatusCode(errorCode)
                                               .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8"))
                .end(Json.encodePrettily(result));
        } catch (Exception e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
            routingContext.response()
                          .setStatusCode(500)
                          .end();
        }
    }

    private HttpServerResponse setCommonHeader(HttpServerResponse response) {
        return response.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    }

    protected void routeResultJson(RoutingContext routingContext, Object object) {
        routingContext.response()
                      .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                      .end(Json.encodePrettily(DataResult.success(object)));
    }

    protected void routeResultJson(RoutingContext routingContext) {
        this.routeResultJson(routingContext, null);
    }

    protected void routeErrorHandler(RoutingContext routingContext, String message) {
         this.routeErrorHandler(routingContext, new Exception(message));
    }

    protected void routeErrorHandler(RoutingContext routingContext, Throwable throwable) {
        this.exceptionHandler(routingContext, 500, throwable);
    }

    protected void routeBadRequestHandler(RoutingContext routingContext, String message) {
         this.routeBadRequestHandler(routingContext, new RuntimeException(message));
    }

    protected void routeBadRequestHandler(RoutingContext routingContext, Throwable throwable) {
        this.exceptionHandler(routingContext, 400, throwable);
    }

    protected void exceptionHandler(RoutingContext routingContext, int statusCode, Throwable throwable) {
        HttpServerResponse response = routingContext.response()
                .setStatusCode(statusCode)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        final Throwable cause = throwable.getCause();
        final String message = cause == null? throwable.getMessage() : cause.getMessage();
        this.setCommonHeader(response).end(Json.encodePrettily(DataResult.fail(message)));
    }
}
