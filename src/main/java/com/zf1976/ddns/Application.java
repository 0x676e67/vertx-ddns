package com.zf1976.ddns;

import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.verticle.DeployVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author mac
 * 2021/7/6
 */
public class Application {

    public static void main(String[] args) {
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
