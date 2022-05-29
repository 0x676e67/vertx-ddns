/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.verticle.provider.impl;

import com.gngpp.ddns.util.ObjectUtil;
import com.gngpp.ddns.util.RsaUtil;
import com.gngpp.ddns.verticle.provider.SecureProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.impl.UserImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author ant
 * Create by Ant on 2021/8/4 1:13 AM
 */
public record UsernamePasswordAuthenticationProvider(
        SecureProvider secureProvider) implements AuthenticationProvider {

    private static final Logger log = LogManager.getLogger("[UsernamePasswordAuthenticationProvider]");
    private static final String usernameKey = "username";
    private static final String passwordKey = "password";

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
        this.secureProvider.readRsaKeyPair()
                           .compose(rsaKeyPair -> this.secureProvider.readLoginConfig()
                                                                     .compose(loginConfig -> this.checkAuthentication(loginConfig, user, rsaKeyPair)))
                           .onComplete(event -> {
                               if (event.succeeded()) {
                                   resultHandler.handle(Future.succeededFuture(user));
                               } else {
                                   resultHandler.handle(Future.failedFuture(event.cause()));
                               }
                           });
    }

    private Future<User> checkAuthentication(Map<String, String> usernamePassword, User user, RsaUtil.RsaKeyPair rsaKeyPair) {

        try {
            final var decodeUsername = RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), user.get(usernameKey));
            final var decodePassword = RsaUtil.decryptByPrivateKey(rsaKeyPair.getPrivateKey(), user.get(passwordKey));
            final var jsonObject = new JsonObject()
                    .put(usernameKey, decodeUsername.trim())
                    .put(passwordKey, decodePassword.trim());
            return this.checkUser(usernamePassword, new UserImpl(jsonObject));
        } catch (Exception e) {
            return Future.failedFuture("server decryption verification error!");
        }
    }

    private Future<User> checkUser(Map<String, String> usernamePassword, User user) {
        final var username = (String) user.get(usernameKey);
        final var password = (String) user.get(passwordKey);
        if (ObjectUtil.nullSafeEquals(usernamePassword.get(usernameKey), username)
                && ObjectUtil.nullSafeEquals(usernamePassword.get(passwordKey), password)) {
            log.info("login in username: {}", username);
            return Future.succeededFuture(user);
        }
        log.error("wrong user name or password!");
        return Future.failedFuture("wrong user name or password!");
    }

}
