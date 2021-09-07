package com.zf1976.ddns.verticle;

import com.zf1976.ddns.api.signer.algorithm.Signer;
import com.zf1976.ddns.config.DnsConfig;
import com.zf1976.ddns.config.SecureConfig;
import com.zf1976.ddns.config.webhook.DingDingMessage;
import com.zf1976.ddns.config.webhook.ServerJMessage;
import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.enums.DnsRecordType;
import com.zf1976.ddns.enums.WebhookProviderType;
import com.zf1976.ddns.pojo.DataResult;
import com.zf1976.ddns.pojo.DnsRecordLog;
import com.zf1976.ddns.util.*;
import com.zf1976.ddns.verticle.handler.LogCacheHandler;
import com.zf1976.ddns.verticle.handler.LogCacheHandlerImpl;
import com.zf1976.ddns.verticle.handler.webhook.CompositeWebhookHandler;
import com.zf1976.ddns.verticle.provider.RedirectAuthenticationProvider;
import com.zf1976.ddns.verticle.provider.UsernamePasswordAuthenticationProvider;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author mac
 * 2021/7/6
 */
public class WebServerVerticle extends AbstractWebServerVerticle {

    private final Logger log = LogManager.getLogger("[ApiVerticle]");
    private WebClient webClient;
    private LogCacheHandler<DnsProviderType, DnsRecordLog> logCacheHandler;

    @Override
    public void start(Promise<Void> startPromise) {
        final var serverPort = serverPort();
        final var router = getRouter();
        final var sessionStore = LocalSessionStore.create(vertx, ApiConstants.SESSION_NAME);
        final var sessionHandler = SessionHandler.create(sessionStore)
                                                 .setLazySession(true)
                                                 // Same site strategy using strict mode
                                                 .setCookieSameSite(CookieSameSite.STRICT);
        final var formLoginHandler = FormLoginHandler.create(new UsernamePasswordAuthenticationProvider(this))
                                                     .setDirectLoggedInOKURL(ApiConstants.INDEX_PATH)
                                                     .setReturnURLParam(ApiConstants.INDEX_PATH);
        SockJSHandlerOptions options = new SockJSHandlerOptions()
                .setRegisterWriteHandler(true)
                .setHeartbeatInterval(2000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        router.mountSubRouter("/api/logs", this.dnsRecordLogHandler(sockJSHandler));

        // all routes use session
        router.route()
              .handler(this::allowWanAccessHandler)
              .handler(XFrameHandler.create(XFrameHandler.DENY))
              .handler(sessionHandler)
              .failureHandler(this::routeErrorHandler);
        final var redirectAuthHandler = RedirectAuthHandler.create(
                new RedirectAuthenticationProvider(),
                ApiConstants.LOGIN_PATH
        );
        // redirect authentication
        router.route("/api/*")
              .handler(redirectAuthHandler);
        router.route("/index.html")
              .handler(redirectAuthHandler);
        // the page must have POST form login data
        router.post("/login")
              .handler(BodyHandler.create())
              .handler(formLoginHandler);
        // sign out
        router.post("/logout")
              .handler(redirectAuthHandler)
              .handler(this::logoutHandler);
        // store DNS service provider key
        router.post("/api/store/dns/config")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .blockingHandler(this::storeDnsConfigHandle);
        // sava secure config
        router.post("/api/store/secure/config")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .handler(this::storeSecureConfigHandler);
        // query DNS service provider's domain name resolution record
        router.post("/api/dns/record/list")
              .handler(this::findDnsRecordsHandler);
        // delete analysis record
        router.delete("/api/dns/record")
              .handler(this::deleteDnsRecordHandler);
        // resolve dns record
        router.post("/api/dns/record/resolve")
              .handler(this::resolveDnsRecordHandler);
        // clear resolve dns record log
        router.delete("/api/dns/record/log")
              .handler(this::clearDnsRecordLogHandler);
        // store webhook config
        router.post("/api/webhook/config")
              .consumes("application/json")
              .handler(BodyHandler.create())
              .handler(this::storeWebhookConfigHandler);
        // send test webhook
        router.post("/api/webhook/test")
              .handler(BodyHandler.create())
              .handler(this::sendWebhookTest);
        // obtain the RSA public key
        router.get("/common/rsa/public_key")
              .handler(this::readRsaPublicKeyHandler);
        // initial configuration
        this.initConfig(vertx)
            .compose(v -> vertx.createHttpServer()
                               .exceptionHandler(Throwable::printStackTrace)
                               .requestHandler(router)
                               .listen(serverPort)
            )
            .onSuccess(event -> {
                log.info("Vertx web server initialized with port(s):{}(http)", serverPort);
                log.info("Vertx-DDNS is running at http://localhost:{}", serverPort);
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
        this.webClient = WebClient.create(vertx);
        this.logCacheHandler = new LogCacheHandlerImpl(vertx);
        this.vertx.deployVerticle(new PeriodicVerticle(this.dnsRecordService,
                this.logCacheHandler,
                new CompositeWebhookHandler(vertx, this)
        ), event -> {
            if (event.succeeded()) {
                context.put(ApiConstants.VERTICLE_PERIODIC_DEPLOY_ID, event.result());
                log.info("PeriodicVerticle deploy complete!");
            } else {
                final var err = event.cause();
                err.printStackTrace();
                log.error("Class：" + err.getClass() + " => Message：" + err.getMessage());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        final var o = context.get(ApiConstants.VERTICLE_PERIODIC_DEPLOY_ID);
        String periodicDeployId = (String) o;
        this.vertx.undeploy(periodicDeployId, event -> {
            if (event.succeeded()) {
                try {
                    super.stop(stopPromise);
                } catch (Exception e) {
                    log.error(e.getMessage(), e.getCause());
                }
            } else {
                log.error(event.cause());
            }
        });
    }

    protected void storeWebhookConfigHandler(RoutingContext ctx) {
        final var request = ctx.request();
        try {
            final var providerType = WebhookProviderType.checkType(request.getParam("type"));
            switch (providerType) {
                case SERVER_J -> {
                    final var serverJMessage = ctx.getBodyAsJson().mapTo(ServerJMessage.class);
                    final var decodeUrl = RsaUtil.decryptByPrivateKey(this.rsaKeyPair.getPrivateKey(), serverJMessage.getUrl());
                    serverJMessage.setUrl(decodeUrl);
                    Validator.of(serverJMessage)
                             .withValidated(v -> HttpUtil.isURL(v.getUrl()), "this is not url");
                    this.readWebhookConfig()
                        .compose(webhookConfig -> {
                            webhookConfig.setServerJMessage(serverJMessage);
                            return this.writeWebhookConfig(webhookConfig);
                        })
                        .onSuccess(v -> this.routeSuccessHandler(ctx))
                        .onFailure(err -> this.routeBadRequestHandler(ctx, err.getMessage()));
                }
                case DING_DING -> {
                    final var dingDingMessage = ctx.getBodyAsJson().mapTo(DingDingMessage.class);
                    Validator.of(dingDingMessage)
                             .withValidated(v -> HttpUtil.isURL(v.getUrl()), "this is not url")
                             .withValidated(v -> !StringUtil.isEmpty(v.getSecret()), "title webhook url secret cannot been empty");
                    this.readWebhookConfig()
                        .compose(webhookConfig -> {
                            final var dingDingMessageList = webhookConfig.getDingDingMessageList();
                            dingDingMessageList.removeIf(v -> v.getMsgType().equals(dingDingMessage.getMsgType()));
                            dingDingMessageList.add(dingDingMessage);
                            return this.writeWebhookConfig(webhookConfig);
                        })
                        .onSuccess(v -> this.routeSuccessHandler(ctx))
                        .onFailure(err -> this.routeBadRequestHandler(ctx, err.getMessage()));
                }
            }
        } catch (Exception e) {
            this.routeBadRequestHandler(ctx, e.getMessage());
        }
    }

    /**
     * Webhook test
     *
     * @param ctx router context
     */
    protected void sendWebhookTest(RoutingContext ctx) {
        final var request = ctx.request();
        try {
            final var webhookProviderType = WebhookProviderType.checkType(request.getParam("type"));
            final var url = AesUtil.decodeByCBC(request.getParam("url"), aesKey.getKey(), aesKey.getIv());
            switch (webhookProviderType) {
                case SERVER_J -> this.webClient.postAbs(url)
                                           .send()
                                           .onSuccess(event -> this.routeSuccessHandler(ctx, "the message has been sent, please check it"))
                                           .onFailure(err -> this.routeErrorHandler(ctx, err.getMessage()));
                case DING_DING -> {
                    final var secret = AesUtil.decodeByCBC(request.getParam("secret"), aesKey.getKey(), aesKey.getIv());
                    long timestamp = System.currentTimeMillis();
                    String stringToSign = timestamp + "\n" + secret;
                    var signData = Signer.getSHA256Signer()
                                         .signString(stringToSign, secret);
                    String sign = ApiURLEncoderUtil.encode(new String(Base64.encodeBase64(signData)));
                    final String completeUrl = url + "&timestamp=" + timestamp + "&sign=" + sign;
                    this.webClient.postAbs(completeUrl)
                                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                  .sendBuffer(ctx.getBody())
                                  .onSuccess(event -> this.routeSuccessHandler(ctx, "the message has been sent, please check it"))
                                  .onFailure(err -> this.routeErrorHandler(ctx, err.getMessage()));
                }
                default -> throw new UnsupportedOperationException("not support：" + webhookProviderType + "message type");
            }

        } catch (Exception e) {
            this.routeBadRequestHandler(ctx, e.getMessage());
        }
    }

    /**
     * sockJS handler
     *
     * @param sockJSHandler sockJs
     * @return {@link Router}
     */
    private Router dnsRecordLogHandler(SockJSHandler sockJSHandler) {
        return sockJSHandler.socketHandler(socket -> vertx.sharedData()
                                                      .getLocalAsyncMap(ApiConstants.SHARE_MAP_ID)
                                                      .compose(shareMap -> {
                                                          socket.handler(providerType -> {
                                                                    final DnsProviderType dnsProviderType;
                                                                    try {
                                                                        dnsProviderType = DnsProviderType.checkType(providerType.toString());
                                                                    } catch (Exception e) {
                                                                        socket.write(e.getMessage());
                                                                        return;
                                                                    }
                                                                    this.logCacheHandler.get(dnsProviderType)
                                                                                        .compose(collection -> shareMap.put(ApiConstants.SOCKJS_SELECT_PROVIDER_TYPE, dnsProviderType)
                                                                                                                          .compose(v -> socket.write(Json.encodePrettily(collection))))
                                                                                        .onFailure(err -> log.error(err.getMessage(), err.getCause()));
                                                                })
                                                                .exceptionHandler(log::error);
                                                          if (socket.writeHandlerID() != null) {
                                                              return shareMap.put(ApiConstants.SOCKJS_WRITE_HANDLER_ID, socket.writeHandlerID());
                                                          }
                                                          return Future.failedFuture("No authentication, please log in for authentication");
                                                      })
                                                      .onFailure(err -> log.error(err.getMessage(), err.getCause())));
    }

    /**
     * not allow wan access handler
     *
     * @param ctx routing context
     */
    protected void allowWanAccessHandler(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        // suspension request
        request.pause();
        this.readSecureConfig()
            .onSuccess(webhookConfig -> {
                // recovery request
                request.resume();
                String ipAddress = HttpUtil.getIpAddress(request);
                if (!webhookConfig.getNotAllowWanAccess() || HttpUtil.isInnerIp(ipAddress)) {
                    ctx.next();
                } else {
                    this.routeBadRequestHandler(ctx, "Prohibit WAN access！");
                }
            })
            .onFailure(err -> log.error(err.getMessage(), err.getCause()));
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
                    this.routeSuccessHandler(ctx, "Sign out successfully！");
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
    protected void readRsaPublicKeyHandler(RoutingContext ctx) {
        this.readRsaKeyPair()
            .onSuccess(rsaKeyPair -> this.routeSuccessHandler(ctx, rsaKeyPair.getPublicKey()))
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
            this.dnsRecordService.findRecordListAsync(dnsProviderType, domain, dnsRecordType)
                                 .onSuccess(bool -> this.routeSuccessHandler(ctx, bool))
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
            this.dnsRecordService.deleteRecordAsync(dnsProviderType, recordId, domain)
                                 .onSuccess(bool -> this.routeSuccessHandler(ctx, bool))
                                 .onFailure(err -> this.routeBadRequestHandler(ctx, err));
        } catch (Exception e) {
            this.routeBadRequestHandler(ctx, "Parameter error");
        }
    }

    /**
     * clear log
     *
     * @param ctx routing context
     */
    protected void clearDnsRecordLogHandler(RoutingContext ctx) {
        final var type = ctx.request()
                            .getParam("type");
        try {
            final var dnsProviderType = DnsProviderType.checkType(type);
            this.logCacheHandler.clear(dnsProviderType)
                                .onSuccess(event -> this.routeSuccessHandler(ctx))
                                .onFailure(err -> this.routeErrorHandler(ctx, err.getMessage()));
        } catch (Exception e) {
            this.routeBadRequestHandler(ctx, e.getMessage());
        }

    }

    /**
     * resolve dns record
     *
     * @param ctx routing context
     */
    protected void resolveDnsRecordHandler(RoutingContext ctx) {
        this.dnsRecordService.update();
        this.routeSuccessHandler(ctx);
    }

    /**
     * store secure config handler
     *
     * @param ctx routing context
     */
    protected void storeSecureConfigHandler(RoutingContext ctx) {
        SecureConfig secureConfig;
        try {
            secureConfig = ctx.getBodyAsJson()
                              .mapTo(SecureConfig.class);
            Validator.of(secureConfig)
                     .withValidated(v -> StringUtil.hasLength(v.getUsername()), "username cannot been empty!")
                     .withValidated(v -> StringUtil.hasLength(v.getPassword()), "password cannot been empty!");
            this.secureConfigDecrypt(secureConfig)
                .compose(this::writeSecureConfig)
                .onSuccess(success -> {
                    ctx.clearUser();
                    // return login url
                    this.routeSuccessHandler(ctx, DataResult.success(ApiConstants.LOGIN_PATH));
                })
                .onFailure(err -> this.routeErrorHandler(ctx, err));
        } catch (Exception e) {
            this.routeErrorHandler(ctx, e.getMessage());
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
            this.dnsConfigDecrypt(dnsConfig)
                .compose(this::writeDnsConfig)
                .onSuccess(success -> this.routeSuccessHandler(ctx))
                .onFailure(err -> this.routeErrorHandler(ctx, err));
        } catch (Exception exception) {
            this.routeBadRequestHandler(ctx, "Parameter abnormal");
        }
    }

}
