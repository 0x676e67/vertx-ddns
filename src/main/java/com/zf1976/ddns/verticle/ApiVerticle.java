package com.zf1976.ddns.verticle;

import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.pojo.DDNSConfig;
import com.zf1976.ddns.pojo.SecureConfig;
import com.zf1976.ddns.util.*;
import com.zf1976.ddns.verticle.auth.RedirectAuthenticationProvider;
import com.zf1976.ddns.verticle.auth.UsernamePasswordAuthenticationProvider;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/6
 */
public class ApiVerticle extends TemplateVerticle {

    private final Logger log = LogManager.getLogger(ApiVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        final var serverPort = serverPort();
        final var router = getRouter();
        final var httpServer = vertx.createHttpServer()
                                    .exceptionHandler(Throwable::printStackTrace);
        final var sessionStore = LocalSessionStore.create(vertx, ApiConstants.SESSION_NAME);
        final var sessionHandler = SessionHandler.create(sessionStore)
                                                 .setLazySession(true)
                                                 // 使用严格模式的同站策略
                                                 .setCookieSameSite(CookieSameSite.STRICT)
                                                 .setCookieSecureFlag(true);
        final var formLoginHandler = FormLoginHandler.create(new UsernamePasswordAuthenticationProvider(this))
                                                     .setDirectLoggedInOKURL(ApiConstants.INDEX_PATH);
        // All routes use session
        router.route()
              .handler(sessionHandler)
              .failureHandler(this::returnError);
        final var redirectAuthHandler = RedirectAuthHandler.create(new RedirectAuthenticationProvider(), ApiConstants.LOGIN_PATH);
        // Redirect authentication
        router.route("/api/*")
              .handler(redirectAuthHandler);
        router.route("/index.html")
              .handler(redirectAuthHandler);
        // The page must have POST form login data
        router.post("/login")
              .handler(BodyHandler.create())
              .handler(formLoginHandler);
        // Sign out
        router.post("/logout")
              .handler(this::logoutHandler);
        // Store DNS service provider key
        router.post("/api/storeConfig")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .handler(this::storeDDNSConfigHandle);
        // sava secure config
        router.post("/api/storeSecureConfig")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .handler(this::storeSecureConfigHandler);
        // Query DNS service provider's domain name resolution record
        router.post("/api/ddnsRecord")
              .handler(this::findDDNSRecordsHandler);
        // Delete analysis record
        router.delete("/api/ddnsRecord")
              .handler(this::deleteDDNSRecordHandler);
        // Obtain the RSA public key
        router.get("/common/rsa/publicKey")
              .handler(this::getRsaPublicKeyHandler);
        // Initial configuration
        this.initConfig(vertx)
            .compose(v -> httpServer.requestHandler(router)
                                    .listen(serverPort))
            .onSuccess(event -> {
                log.info("Vertx web server initialized with port(s): " + serverPort + " (http)");
                log.info("DDNS-Vertx is running at http://localhost:" + serverPort);
                try {
                    super.start(startPromise);
                } catch (Exception e) {
                    startPromise.fail(e);
                }
            })
            .onFailure(startPromise::fail);
    }

    @Override
    public void start() throws Exception {
//        vertx.setPeriodic(1000, id -> {
//            final var context = vertx.getOrCreateContext();
//            final var periodicId = context.get(ApiConstants.PERIODIC);
//            if (periodicId == null) {
//                context.put(ApiConstants.PERIODIC, id);
//            } else {
//                System.out.println(periodicId instanceof Long);
//                System.out.println(periodicId);
//            }
//        });
    }

    protected void logoutHandler(RoutingContext ctx) {
        if (ctx.user() != null) {
            final var session = ctx.session();
            final var id = session.id();
            for (Map.Entry<String, Cookie> cookieEntry : ctx.cookieMap()
                                                            .entrySet()) {
                final var cookie = cookieEntry.getValue();
                if (ObjectUtil.nullSafeEquals(id, cookie.getValue())) {
                    ctx.clearUser();
                    this.returnJson(ctx, "Sign out successfully！");
                    break;
                }
            }
        } else {
            this.handleBadRequest(ctx, "Invalid request");
        }
    }

    protected void getRsaPublicKeyHandler(RoutingContext ctx) {
        this.readRsaKeyPair()
            .onSuccess(rsaKeyPair -> this.returnJson(ctx, rsaKeyPair.getPublicKey()))
            .onFailure(err -> this.handleErrorRequest(ctx, err));
    }

    protected void findDDNSRecordsHandler(RoutingContext routingContext) {
        try {
            final var request = routingContext.request();
            final var ipRecordType = DNSRecordType.checkType(request.getParam(ApiConstants.IP_RECORD_TYPE));
            final var dnsServiceType = DNSServiceType.checkType(request.getParam(ApiConstants.DDNS_SERVICE_TYPE));
            final var domain = request.getParam(ApiConstants.DOMAIN);
            final var dataResult = this.dnsConfigTimerService.findDnsRecords(dnsServiceType, domain, ipRecordType);
            this.returnJson(routingContext, dataResult);
        } catch (RuntimeException exception) {
            this.handleBadRequest(routingContext, exception);
        } catch (Exception exception) {
            this.handleErrorRequest(routingContext, new RuntimeException("Parameter abnormal"));
        }
    }

    protected void deleteDDNSRecordHandler(RoutingContext routingContext) {
        try {
            final var request = routingContext.request();
            final var recordId = request.getParam(ApiConstants.RECORD_ID);
            final var dnsServiceType = DNSServiceType.checkType(request.getParam(ApiConstants.DDNS_SERVICE_TYPE));
            final var domain = request.getParam(ApiConstants.DOMAIN);
            final var success = this.dnsConfigTimerService.deleteRecords(dnsServiceType, recordId, domain);
            this.returnJson(routingContext, success);
        } catch (Exception e) {
            this.handleBadRequest(routingContext, e.getMessage());
        }
    }

    protected void storeSecureConfigHandler(RoutingContext routingContext) {
        SecureConfig secureConfig;
        try {
            secureConfig = routingContext.getBodyAsJson().mapTo(SecureConfig.class);
            Assert.notNull(secureConfig, "body cannot been null!");
            Assert.hasLength(secureConfig.getUsername(), "username cannot been null!");
            Assert.hasLength(secureConfig.getPassword(), "username cannot been null!");
            this.secureConfigDecryptHandler(secureConfig)
                    .compose(this::storeSecureConfig)
                    .onSuccess(success -> this.returnJson(routingContext))
                    .onFailure(err -> this.handleErrorRequest(routingContext, err));
        } catch (Exception e) {
            this.handleErrorRequest(routingContext, new RuntimeException("Parameter abnormal"));
        }
    }

    protected void storeDDNSConfigHandle(RoutingContext routingContext) {
        try {
            final var ddnsConfig = routingContext.getBodyAsJson().mapTo(DDNSConfig.class);
            switch (ddnsConfig.getDnsServiceType()) {
                case ALIYUN:
                case HUAWEI:
                case DNSPOD:
                    Validator.of(ddnsConfig)
                             .withValidated(v -> !StringUtil.isEmpty(v.getId()) && !v.getId().isBlank(), "ID cannot be empty")
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret().isBlank(), "The Secret cannot be empty");
                    break;
                case CLOUDFLARE:
                    Validator.of(ddnsConfig)
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret().isBlank(), "The Secret cannot be empty");
                default:
            }
            this.ddnsConfigDecryptHandler(ddnsConfig)
                    .compose(this::storeDDNSConfig)
                    .onSuccess(success -> this.returnJson(routingContext))
                    .onFailure(err -> this.handleErrorRequest(routingContext, err));
        } catch (Exception exception) {
            this.handleBadRequest(routingContext, new RuntimeException("Parameter abnormal"));
        }
    }

    private Future<Void> storeDDNSConfig(DDNSConfig config) {
        final var fileSystem = vertx.fileSystem();
        final String absolutePath = this.toAbsolutePath(workDir, DDNS_CONFIG_FILENAME);
        return this.readDDNSConfig(fileSystem)
                   .compose(configList -> this.writeDDNSConfig(configList, config, absolutePath)
                                              .compose(v -> newDnsConfigTimerService(configList)));
    }

    private Future<Void> storeSecureConfig(SecureConfig secureConfig) {
        String absolutePath = this.toAbsolutePath(workDir, SECURE_CONFIG_FILENAME);
        return this.writeConfig(absolutePath, Json.encodePrettily(secureConfig));
    }

    private Future<Void> writeDDNSConfig(List<DDNSConfig> ddnsConfigs, DDNSConfig ddnsConfig, String configFilePath) {
        // 读取配置为空
        if (CollectionUtil.isEmpty(ddnsConfigs)) {
            List<DDNSConfig> accountList = new ArrayList<>();
            accountList.add(ddnsConfig);
            return this.writeConfig(configFilePath, Json.encodePrettily(accountList));
        } else {
            try {
                ddnsConfigs.removeIf(config -> ddnsConfig.getDnsServiceType().equals(config.getDnsServiceType()));
                ddnsConfigs.add(ddnsConfig);
                return this.writeConfig(configFilePath, Json.encodePrettily(ddnsConfigs));
            } catch (Exception e) {
                return Future.failedFuture(new RuntimeException("Server Error"));
            }
        }
    }

    private Future<Void> writeConfig(String configFilePath, String json) {
        return vertx.fileSystem()
                    .writeFile(configFilePath, Buffer.buffer(json));
    }


}
