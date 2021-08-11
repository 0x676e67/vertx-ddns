package com.zf1976.ddns.verticle.auth;

import com.zf1976.ddns.pojo.SecureConfig;
import com.zf1976.ddns.util.ObjectUtil;
import com.zf1976.ddns.util.RsaUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.impl.UserImpl;

/**
 * @author ant
 * Create by Ant on 2021/8/4 1:13 AM
 */
public record UsernamePasswordAuthenticationProvider(
        SecureProvider secureHandler) implements AuthenticationProvider {

    /**
     * Authenticate a user.
     * <p>
     * The first argument is a JSON object containing information for authenticating the user. What this actually contains
     * depends on the specific implementation. In the case of a simple username/password based
     * authentication it is likely to contain a JSON object with the following structure:
     * <pre>
     *   {
     *     "username": "tim",
     *     "password": "mypassword"
     *   }
     * </pre>
     * For other types of authentication it contain different information - for example a JWT token or OAuth bearer token.
     * <p>
     * If the user is successfully authenticated a {@link User} object is passed to the handler in an {@link AsyncResult}.
     * The user object can then be used for authorisation.
     *
     * @param credentials   The credentials
     * @param resultHandler The result handler
     */
    @Override
    public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
        final var user = new UserImpl(credentials);
        this.secureHandler.readRsaKeyPair()
                .compose(rsaKeyPair -> this.secureHandler.readSecureConfig()
                        .compose(secureConfig -> this.checkAuthentication(secureConfig, user, rsaKeyPair)))
                .onComplete(event -> {
                    if (event.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(user));
                    } else {
                        resultHandler.handle(Future.failedFuture(event.cause()));
                    }
                });
    }

    private Future<User> checkAuthentication(SecureConfig secureConfig, User user, RsaUtil.RsaKeyPair rsaKeyPair) {
        if (secureConfig == null) {
            return Future.succeededFuture(user);
        }
        try {
            final var checkUsername = RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), user.get("username"));
            final var checkPassword = RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), user.get("password"));
            if (ObjectUtil.nullSafeEquals(secureConfig.getUsername(), checkUsername) && ObjectUtil.nullSafeEquals(secureConfig.getPassword(), checkPassword)) {
                return Future.succeededFuture(user);
            }
            return Future.failedFuture("wrong user name or password!");
        } catch (Exception e) {
            return Future.failedFuture("server decryption verification error!");
        }
    }

}
