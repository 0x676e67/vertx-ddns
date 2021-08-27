package com.zf1976.ddns.verticle;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.config.DnsConfig;
import com.zf1976.ddns.config.SecureConfig;
import com.zf1976.ddns.config.WebhookConfig;
import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.pojo.DataResult;
import com.zf1976.ddns.util.*;
import com.zf1976.ddns.verticle.provider.SecureProvider;
import com.zf1976.ddns.verticle.provider.WebhookProvider;
import com.zf1976.ddns.verticle.timer.service.DnsRecordService;
import com.zf1976.ddns.verticle.timer.service.impl.DnsRecordServiceImpl;
import io.vertx.core.*;
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
import java.util.*;

/**
 * @author mac
 * 2021/7/7
 */
public abstract class AbstractApiVerticle extends AbstractVerticle implements SecureProvider, WebhookProvider {

    private final Logger log = LogManager.getLogger("[TemplateVerticle]");
    private volatile static Router router;
    protected String workDir = null;
    protected static final String WORK_DIR_NAME = ".vertx_ddns";
    protected static final String DNS_CONFIG_FILENAME = "dns_config.json";
    protected static final String SECURE_CONFIG_FILENAME = "secure_config.json";
    protected static final String WEBHOOK_CONFIG_FILENAME = "webhook_config.json";
    protected static final String RSA_KEY_FILENAME = "rsa_key.json";
    protected static final String AES_KEY_FILENAME = "aes_key.json";
    protected RsaUtil.RsaKeyPair rsaKeyPair;
    protected AesUtil.AesKey aesKey;
    protected DnsRecordService dnsRecordService;
    protected Boolean notAllowWanAccess = Boolean.TRUE;
    protected SecureConfig defaultSecureConfig;

    public AbstractApiVerticle() {
        this.defaultSecureConfig = ConfigProperty.getDefaultSecureConfig();
    }

    protected synchronized Router getRouter() {
        return router;
    }

    protected Integer serverPort() {
        return Integer.valueOf(config().getString(ApiConstants.SERVER_PORT));
    }

    @Override
    public void init(Vertx vertx, Context context) {
        if (router == null) {
            synchronized (AbstractApiVerticle.class) {
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
         final var dnsConfigFilePath = this.toAbsolutePath(projectWorkPath, DNS_CONFIG_FILENAME);
         final var secureFilePath = this.toAbsolutePath(projectWorkPath, SECURE_CONFIG_FILENAME);
         final var webhookFilePath = this.toAbsolutePath(projectWorkPath, WEBHOOK_CONFIG_FILENAME);
         final var rsaKeyPath = this.toAbsolutePath(projectWorkPath, RSA_KEY_FILENAME);
         final var aesKeyPath = this.toAbsolutePath(projectWorkPath, AES_KEY_FILENAME);
         this.workDir = projectWorkPath;
         return fileSystem.mkdirs(projectWorkPath)
                          .compose(v -> fileSystem.exists(dnsConfigFilePath))
                          .compose(bool -> createFile(fileSystem, bool, dnsConfigFilePath))
                          .compose(v -> fileSystem.exists(secureFilePath))
                          .compose(bool -> createFile(fileSystem, bool, secureFilePath))
                          .compose(v -> fileSystem.exists(webhookFilePath))
                          .compose(bool -> createFile(fileSystem, bool, webhookFilePath))
                          .compose(v -> {
                              final var rsaKeyPairFuture = fileSystem.exists(rsaKeyPath)
                                                                     .compose(bool -> createRsaKeyFile(fileSystem, bool, rsaKeyPath))
                                                                     .compose(rsa -> this.readRsaKeyPair())
                                                                     .onSuccess(key -> {
                                                                         this.rsaKeyPair = key;
                                                                     });

                              final var aesKeyFuture = fileSystem.exists(aesKeyPath)
                                                                 .compose(bool -> createAesKeyFile(fileSystem, bool, aesKeyPath))
                                                                 .compose(aes -> this.readAesKey())
                                                                 .onSuccess(key -> {
                                                                     this.aesKey = key;
                                                                 });
                              return CompositeFuture.all(aesKeyFuture, rsaKeyPairFuture);

                          })
                          .compose(v -> {
                              log.info("Initialize project working directory：" + projectWorkPath);
                              log.info("Initialize DNS configuration file：" + dnsConfigFilePath);
                              log.info("Initialize secure configuration file：" + secureFilePath);
                              log.info("Initialize webhook configuration file：" + webhookFilePath);
                              log.info("Initialize rsa key configuration file：" + rsaKeyPath);
                              log.info("Initialize aes key configuration file：" + aesKeyPath);
                              log.info("RSA key has been initialized");
                              log.info("AES key has been initialized");
                              this.routeTemplateHandler(router, vertx);
                              return this.initDnsServiceConfig(vertx.fileSystem());
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
        if (!ctx.request().absoluteURI().contains(ApiConstants.LOGIN_PATH) && ctx.user() == null) {
            ctx.redirect(ApiConstants.LOGIN_PATH);
            return;
        }
        this.readDnsConfig()
            .compose(dnsConfigList -> {
                if (!CollectionUtil.isEmpty(dnsConfigList)) {
                    for (DnsConfig dnsConfig : dnsConfigList) {
                        dnsConfig.setId(this.hideHandler(dnsConfig.getId()))
                                 .setSecret(this.hideHandler(dnsConfig.getSecret()));
                    }
                }
                ctx.put("dnsConfigList", dnsConfigList);
                return this.readRsaKeyPair();
            })
            .compose(rsaKeyPair -> {
                ctx.put("rsaPublicKey", rsaKeyPair.getPublicKey());
                return this.readAesKey();
            })
            .compose(aesKey -> {
                ctx.put("aesKey", aesKey);
                return this.readSecureConfig();
            })
            .compose(secureConfig -> this.hidePasswordHandler(ctx, secureConfig))
            .compose(v -> this.readWebhookConfig())
            .onSuccess(webhookConfig -> {
                ctx.put("common", ConfigProperty.getDefaultProperties())
                   .put("webhookConfig", webhookConfig)
                   .put("ipv4", HttpUtil.getNetworkIpv4List())
                   .put("ipv6", HttpUtil.getNetworkIpv6List());
                templateHandler.handle(ctx);
            })
            .onFailure(err -> this.routeErrorHandler(ctx, err));
    }

    protected Future<Void> hidePasswordHandler(RoutingContext ctx, SecureConfig secureConfig) {
        final var hidePassword = this.hideHandler(secureConfig.getPassword());
        try {
            final var clone = (SecureConfig) secureConfig.clone();
            clone.setPassword(hidePassword);
            ctx.put("secureConfig", clone);
            return Future.succeededFuture();
        } catch (CloneNotSupportedException e) {
            return Future.failedFuture("Server error!");
        }
    }

    protected Future<Void> initDnsServiceConfig(FileSystem fileSystem) {
        return this.readDnsConfig()
                   .compose(this::newDnsRecordService);
    }

    protected Future<Void> newDnsRecordService(List<DnsConfig> dnsConfigList) {
        try {
            if (Objects.isNull(this.dnsRecordService)) {
                this.dnsRecordService = new DnsRecordServiceImpl(dnsConfigList, this.vertx);
            } else {
                this.dnsRecordService.reloadProviderCredentials(dnsConfigList);
            }
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

    private Future<Void> writeAesKeyFile(FileSystem fileSystem, String aesKeyPath) {
        try {
            final var aesKey = AesUtil.generateKey();
            this.aesKey = aesKey;
            return fileSystem.writeFile(aesKeyPath, Buffer.buffer(Json.encodePrettily(aesKey)));
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return Future.failedFuture(e);
        }
    }

    /**
     * write config to file
     *
     * @param absolutePath path
     * @param json         JSON
     * @return {@link Future<Void>
     */
    protected Future<Void> writeJsonToFile(String absolutePath, String json) {
        return vertx.fileSystem()
                    .writeFile(absolutePath, Buffer.buffer(json));
    }

    protected Future<Void> writeWebhookConfig(WebhookConfig webhookConfig) {
        final var absolutePath = this.toAbsolutePath(workDir, WEBHOOK_CONFIG_FILENAME);
        return this.writeJsonToFile(absolutePath, Json.encodePrettily(webhookConfig));
    }

    /**
     * store DDNS config to file
     *
     * @param dnsConfig DDNS config
     * @return {@link Future<Void>}
     */
    protected Future<Void> writeDnsConfig(DnsConfig dnsConfig) {
        final String absolutePath = this.toAbsolutePath(workDir, DNS_CONFIG_FILENAME);
        return this.readDnsConfig()
                   .compose(dnsConfigList -> {
                       // 读取配置为空
                       if (CollectionUtil.isEmpty(dnsConfigList)) {
                           List<DnsConfig> newDnsConfigList = new ArrayList<>();
                           newDnsConfigList.add(dnsConfig);
                           return this.writeJsonToFile(absolutePath, Json.encodePrettily(newDnsConfigList));
                       } else {
                           try {
                               dnsConfigList.removeIf(config -> dnsConfig.getDnsProviderType()
                                                                         .equals(config.getDnsProviderType()));
                               dnsConfigList.add(dnsConfig);
                               return this.writeJsonToFile(absolutePath, Json.encodePrettily(dnsConfigList))
                                          .compose(v -> newDnsRecordService(dnsConfigList));
                           } catch (Exception e) {
                               return Future.failedFuture("Server Error");
                           }
                       }
                   });
    }

    /**
     * store secure config to file
     *
     * @param secureConfig secure config
     * @return {@link Future<Void>}
     */
    protected Future<Void> writeSecureConfig(SecureConfig secureConfig) {
        String absolutePath = this.toAbsolutePath(workDir, SECURE_CONFIG_FILENAME);
        return this.writeJsonToFile(absolutePath, Json.encodePrettily(secureConfig))
                   .compose(v -> {
                       this.notAllowWanAccess = secureConfig.getNotAllowWanAccess() == null? Boolean.TRUE : Boolean.FALSE;
                       return Future.succeededFuture();
                   });
    }

    private Future<Void> createFile(FileSystem fileSystem, boolean bool, String path) {
        if (!bool) {
            return fileSystem.createFile(path);
        }
        return Future.succeededFuture();
    }

    private Future<Void> createRsaKeyFile(FileSystem fileSystem, boolean bool, String path) {
        if (!bool) {
            return fileSystem.createFile(path)
                             .compose(v -> writeRsaKeyFile(fileSystem, path));
        }
        return Future.succeededFuture();
    }

    private Future<Void> createAesKeyFile(FileSystem fileSystem, boolean bool, String path) {
        if (!bool) {
            return fileSystem.createFile(path)
                             .compose(v -> writeAesKeyFile(fileSystem, path));
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<AesUtil.AesKey> readAesKey() {
        if (Objects.nonNull(this.aesKey)) {
            return Future.succeededFuture(this.aesKey);
        }
        return vertx.fileSystem()
                    .readFile(toAbsolutePath(workDir, AES_KEY_FILENAME))
                    .compose(buffer -> Future.succeededFuture(Json.decodeValue(buffer, AesUtil.AesKey.class)));
    }

    @Override
    public Future<RsaUtil.RsaKeyPair> readRsaKeyPair() {
        if (Objects.nonNull(this.rsaKeyPair)) {
            return Future.succeededFuture(this.rsaKeyPair);
        }
        return vertx.fileSystem()
                    .readFile(toAbsolutePath(workDir, RSA_KEY_FILENAME))
                    .compose(buffer -> Future.succeededFuture(Json.decodeValue(buffer, RsaUtil.RsaKeyPair.class)));
    }

    @Override
    public Future<Map<String, String>> readLoginConfig() {
        return this.readSecureConfig()
                   .compose(secureConfig -> {
                       final var usernamePasswordMap = new HashMap<String, String>();
                       usernamePasswordMap.put("username", secureConfig.getUsername());
                       usernamePasswordMap.put("password", secureConfig.getPassword());
                       return Future.succeededFuture(usernamePasswordMap);
                   });
    }

    protected Future<SecureConfig> readSecureConfig() {
        String absolutePath = this.toAbsolutePath(workDir, SECURE_CONFIG_FILENAME);
        return vertx.fileSystem()
                    .readFile(absolutePath)
                    .compose(buffer -> {
                        try {
                            // config is empty
                            if (StringUtil.isEmpty(buffer.toString())) {
                                return Future.succeededFuture(this.defaultSecureConfig);
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

    @Override
    public Future<WebhookConfig> readWebhookConfig() {
        String absolutePath = toAbsolutePath(workDir, WEBHOOK_CONFIG_FILENAME);
        return vertx.fileSystem()
                    .readFile(absolutePath)
                    .compose(buffer -> {
                        if (StringUtil.isEmpty(buffer.toString())) {
                            return Future.succeededFuture(new WebhookConfig());
                        } else {
                            final var webhookConfig = Json.decodeValue(buffer, WebhookConfig.class);
                            return Future.succeededFuture(webhookConfig);
                        }
                    });
    }

    protected Future<List<DnsConfig>> readDnsConfig() {
        String absolutePath = toAbsolutePath(workDir, DNS_CONFIG_FILENAME);
        return vertx.fileSystem()
                    .readFile(absolutePath)
                    .compose(buffer -> {
                        try {
                            List<DnsConfig> dnsConfigList = new ArrayList<>();
                            // config is empty
                            if (StringUtil.isEmpty(buffer.toString())) {
                                return Future.succeededFuture(dnsConfigList);
                            }
                            var list = Json.decodeValue(buffer, List.class);
                            if (CollectionUtil.isEmpty(list)) {
                                return Future.succeededFuture(dnsConfigList);
                            }
                            for (Object o : list) {
                                dnsConfigList.add(JsonObject.mapFrom(o)
                                                            .mapTo(DnsConfig.class));
                            }
                            return Future.succeededFuture(dnsConfigList);
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

    protected Future<DnsConfig> dnsConfigDecrypt(DnsConfig dnsConfig) {
        return this.readRsaKeyPair()
                   .compose(keyPair -> this.dnsConfigDecrypt(keyPair, dnsConfig));
    }

    protected Future<SecureConfig> secureConfigDecrypt(SecureConfig secureConfig) {
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

    protected Future<DnsConfig> dnsConfigDecrypt(RsaUtil.RsaKeyPair rsaKeyPair, DnsConfig dnsConfig) {
        if (Objects.isNull(rsaKeyPair)) {
            return Future.failedFuture("RSA keyless");
        }
        try {
            // cloudflare only token is used as access key
            if (!dnsConfig.getDnsProviderType().equals(DnsProviderType.CLOUDFLARE)) {
                String id = RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), dnsConfig.getId());
                dnsConfig.setId(id);
            }
            String secret = RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), dnsConfig.getSecret());
            dnsConfig.setSecret(secret);
            return Future.succeededFuture(dnsConfig);
        } catch (Exception e) {
            return readDnsConfig()
                    .compose(ddnsConfigList -> {
                        for (DnsConfig rawConfig : ddnsConfigList) {
                            if (dnsConfig.getDnsProviderType().equals(rawConfig.getDnsProviderType())) {
                                // cloudflare only token is used as access key
                                if (!dnsConfig.getDnsProviderType().equals(DnsProviderType.CLOUDFLARE)) {
                                    if (this.isHide(rawConfig.getId(), dnsConfig.getId()) && this.isHide(rawConfig.getSecret(), dnsConfig.getSecret())) {
                                        dnsConfig.setId(rawConfig.getId())
                                                .setSecret(rawConfig.getSecret());
                                        return Future.succeededFuture(dnsConfig);
                                    }
                                } else {
                                    if (this.isHide(rawConfig.getSecret(), dnsConfig.getSecret())) {
                                        return Future.succeededFuture(dnsConfig.setSecret(rawConfig.getSecret()));
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
            return StringUtil.EMPTY;
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
            routingContext.response()
                          .setStatusCode(errorCode)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                          .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                          .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                          .send(Json.encodeToBuffer(result));

        } catch (Exception e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
        }
    }

    protected void routeSuccessHandler(RoutingContext routingContext, Object object) {
        routingContext.response()
                      .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                      .end(Json.encodePrettily(DataResult.success(object)));
    }

    protected void routeSuccessHandler(RoutingContext routingContext) {
        this.routeSuccessHandler(routingContext, null);
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
        final var fail = DataResult.fail(message);
        fail.setErrCode(statusCode);
        response.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                .end(Json.encodePrettily(fail));
    }
}
