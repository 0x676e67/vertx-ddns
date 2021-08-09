package com.zf1976.ddns.verticle;

import com.zf1976.ddns.api.enums.DnsRecordType;
import com.zf1976.ddns.api.enums.DnsProviderType;
import com.zf1976.ddns.pojo.DnsConfig;
import com.zf1976.ddns.pojo.SecureConfig;
import com.zf1976.ddns.util.*;
import com.zf1976.ddns.verticle.auth.RedirectAuthenticationProvider;
import com.zf1976.ddns.verticle.auth.UsernamePasswordAuthenticationProvider;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServerRequest;
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
                                                 // Same site strategy using strict mode
                                                 .setCookieSameSite(CookieSameSite.STRICT);
        final var formLoginHandler = FormLoginHandler.create(new UsernamePasswordAuthenticationProvider(this))
                                                     .setDirectLoggedInOKURL(ApiConstants.INDEX_PATH)
                                                     .setReturnURLParam(ApiConstants.INDEX_PATH);
        // All routes use session
        router.route()
              .handler(this::notAllowWanAccessHandler)
              .handler(sessionHandler)
              .failureHandler(this::routeErrorHandler);
        final var redirectAuthHandler = RedirectAuthHandler.create(new RedirectAuthenticationProvider(),
                ApiConstants.LOGIN_PATH,
                ApiConstants.INDEX_PATH);
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
        router.post("/api/store/dns/config")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .blockingHandler(this::storeDnsConfigHandle);
        // sava secure config
        router.post("/api/store/secure/config")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .handler(this::storeSecureConfigHandler);
        // Query DNS service provider's domain name resolution record
        router.post("/api/dns/record/list")
              .handler(this::findDnsRecordsHandler);
        // DELETE analysis record
        router.delete("/api/dns/record")
              .blockingHandler(this::deleteDnsRecordHandler);
        // Obtain the RSA public key
        router.get("/common/rsa/public_key")
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

    /**
     * not allow wan access handler
     *
     * @param ctx routing context
     */
    protected void notAllowWanAccessHandler(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        String ipAddress = HttpUtil.getIpAddress(request);
        if (IpUtil.isInnerIp(ipAddress)) {
            ctx.next();
        } else {
            this.routeBadRequestHandler(ctx, "Prohibit WAN access！");
        }
    }

    /**
     * logout handler
     *
     * @param ctx routing context
     */
    protected void logoutHandler(RoutingContext ctx) {
        if (ctx.user() != null) {
            final var session = ctx.session();
            final var id = session.id();
            for (Map.Entry<String, Cookie> cookieEntry : ctx.cookieMap()
                                                            .entrySet()) {
                final var cookie = cookieEntry.getValue();
                if (ObjectUtil.nullSafeEquals(id, cookie.getValue())) {
                    ctx.clearUser();
                    this.routeResultJson(ctx, "Sign out successfully！");
                    break;
                }
            }
        } else {
            this.routeBadRequestHandler(ctx, "Invalid request");
        }
    }

    /**
     * get rsa public key handler
     *
     * @param ctx routing context
     */
    protected void getRsaPublicKeyHandler(RoutingContext ctx) {
        this.readRsaKeyPair()
            .onSuccess(rsaKeyPair -> this.routeResultJson(ctx, rsaKeyPair.getPublicKey()))
            .onFailure(err -> this.routeErrorHandler(ctx, err));
    }

    /**
     * find DDNS records handler
     *
     * @param ctx routing context
     */
    protected void findDnsRecordsHandler(RoutingContext ctx) {
        try {
            final var request = ctx.request();
            final var dnsRecordType = DnsRecordType.checkType(request.getParam(ApiConstants.DNS_RECORD_TYPE));
            final var dnsProviderType = DnsProviderType.checkType(request.getParam(ApiConstants.DDNS_PROVIDER_TYPE));
            final var domain = request.getParam(ApiConstants.DOMAIN);
            this.dnsConfigTimerService.findDnsRecordListAsync(dnsProviderType, domain, dnsRecordType)
                                      .onSuccess(bool -> this.routeResultJson(ctx, bool))
                                      .onFailure(err -> this.routeBadRequestHandler(ctx, err));
        } catch (Exception e) {
            this.routeErrorHandler(ctx, "Parameter error");
        }
    }

    /**
     * delete DDNS record handler
     *
     * @param ctx routing context
     */
    protected void deleteDnsRecordHandler(RoutingContext ctx) {
        try {
            final var request = ctx.request();
            final var recordId = request.getParam(ApiConstants.RECORD_ID);
            final var dnsProviderType = DnsProviderType.checkType(request.getParam(ApiConstants.DDNS_PROVIDER_TYPE));
            final var domain = request.getParam(ApiConstants.DOMAIN);
            this.dnsConfigTimerService.deleteRecordAsync(dnsProviderType, recordId, domain)
                    .onSuccess(bool -> this.routeResultJson(ctx, bool))
                    .onFailure(err -> this.routeBadRequestHandler(ctx, err));
        } catch (Exception e) {
            this.routeBadRequestHandler(ctx, "Parameter error");
        }
    }

    /**
     * store secure config handler
     *
     * @param ctx routing context
     */
    protected void storeSecureConfigHandler(RoutingContext ctx) {
        SecureConfig secureConfig;
        try {
            secureConfig = ctx.getBodyAsJson().mapTo(SecureConfig.class);
            Assert.notNull(secureConfig, "body cannot been null!");
            Assert.hasLength(secureConfig.getUsername(), "username cannot been null!");
            Assert.hasLength(secureConfig.getPassword(), "username cannot been null!");
            this.secureConfigDecryptHandler(secureConfig)
                    .compose(this::storeSecureConfig)
                    .onSuccess(success -> this.routeResultJson(ctx))
                    .onFailure(err -> this.routeErrorHandler(ctx, err));
        } catch (Exception e) {
            this.routeErrorHandler(ctx, new RuntimeException("Parameter abnormal"));
        }
    }

    /**
     * store DDNS config handler
     *
     * @param ctx routing context
     */
    protected void storeDnsConfigHandle(RoutingContext ctx) {
        try {
            final var dnsConfig = ctx.getBodyAsJson().mapTo(DnsConfig.class);
            switch (dnsConfig.getDnsProviderType()) {
                case ALIYUN:
                case HUAWEI:
                case DNSPOD:
                    Validator.of(dnsConfig)
                             .withValidated(v -> !StringUtil.isEmpty(v.getId()) && !v.getId().isBlank(), "ID cannot be empty")
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret().isBlank(), "The Secret cannot be empty");
                    break;
                case CLOUDFLARE:
                    Validator.of(dnsConfig)
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()) && !v.getSecret().isBlank(), "The Secret cannot be empty");
                default:
            }
            this.dnsConfigDecryptHandler(dnsConfig)
                    .compose(this::storeDnsConfig)
                    .onSuccess(success -> this.routeResultJson(ctx))
                    .onFailure(err -> this.routeErrorHandler(ctx, err));
        } catch (Exception exception) {
            this.routeBadRequestHandler(ctx, "Parameter abnormal");
        }
    }

    /**
     * store DDNS config to file
     *
     * @param dnsConfig DDNS config
     * @return {@link Future<Void>}
     */
    private Future<Void> storeDnsConfig(DnsConfig dnsConfig) {
        final var fileSystem = vertx.fileSystem();
        final String absolutePath = this.toAbsolutePath(workDir, DNS_CONFIG_FILENAME);
        return this.readDnsConfig(fileSystem)
                   .compose(configList -> this.writeDnsConfig(configList, dnsConfig, absolutePath)
                                              .compose(v -> newDnsConfigTimerService(configList)));
    }

    /**
     * store secure config to file
     *
     * @param secureConfig secure config
     * @return {@link Future<Void>}
     */
    private Future<Void> storeSecureConfig(SecureConfig secureConfig) {
        String absolutePath = this.toAbsolutePath(workDir, SECURE_CONFIG_FILENAME);
        return this.writeJsonToFile(absolutePath, Json.encodePrettily(secureConfig))
                .compose(v -> {
                    this.notAllowWanAccess = secureConfig.getNotAllowWanAccess() == null? Boolean.TRUE : Boolean.FALSE;
                    return Future.succeededFuture();
                });
    }

    /**
     * write DDNS config
     *
     * @param dnsConfigList DDNS config list
     * @param dnsConfig new DDNS config
     * @param absolutePath file absolute path
     * @return {@link Future<Void>}
     */
    private Future<Void> writeDnsConfig(List<DnsConfig> dnsConfigList, DnsConfig dnsConfig, String absolutePath) {
        // 读取配置为空
        if (CollectionUtil.isEmpty(dnsConfigList)) {
            List<DnsConfig> newDnsConfigList = new ArrayList<>();
            newDnsConfigList.add(dnsConfig);
            return this.writeJsonToFile(absolutePath, Json.encodePrettily(newDnsConfigList));
        } else {
            try {
                dnsConfigList.removeIf(config -> dnsConfig.getDnsProviderType().equals(config.getDnsProviderType()));
                dnsConfigList.add(dnsConfig);
                return this.writeJsonToFile(absolutePath, Json.encodePrettily(dnsConfigList));
            } catch (Exception e) {
                return Future.failedFuture(new RuntimeException("Server Error"));
            }
        }
    }

    /**
     * write config to file
     *
     * @param absolutePath path
     * @param json JSON
     * @return {@link Future< Void>
     */
    private Future<Void> writeJsonToFile(String absolutePath, String json) {
        return vertx.fileSystem()
                    .writeFile(absolutePath, Buffer.buffer(json));
    }

}
