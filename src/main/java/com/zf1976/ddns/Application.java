/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
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

package com.zf1976.ddns;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.verticle.DeployVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;

import java.io.IOException;

/**
 * @author mac
 * 2021/7/6
 */
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
        final var vertxOptions = new VertxOptions();
        final var addressResolverOptions = new AddressResolverOptions()
                // Server list polling
                .setRotateServers(true);
        for (String dnsServer : ConfigProperty.getDefaultProperties()
                                              .getDnsServerList()) {
            addressResolverOptions.addServer(dnsServer);
        }
        vertxOptions.setAddressResolverOptions(addressResolverOptions);
        Vertx.vertx(vertxOptions)
             .deployVerticle(new DeployVerticle(args));
    }

}
