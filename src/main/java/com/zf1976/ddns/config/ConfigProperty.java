package com.zf1976.ddns.config;

import com.zf1976.ddns.config.property.AliyunDnsProperties;
import com.zf1976.ddns.config.property.DefaultProperties;
import com.zf1976.ddns.util.PropertyUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.BufferedInputStream;

/**
 * @author mac
 * 2021/7/7
 */
public class ConfigProperty {

    private final JsonObject jsonConfig;

    private ConfigProperty() {
        this.jsonConfig = loadLocalConfig();
    }

    private static final class ConfigHolder {
        private static final ConfigProperty config = new ConfigProperty();
    }

    public static ConfigProperty getInstance() {
        return ConfigHolder.config;
    }

    private JsonObject loadLocalConfig() {
        final var resourceAsStream = PropertyUtil.class.getClassLoader()
                                                       .getResourceAsStream("conf.json");
        if (resourceAsStream == null) {
            throw new RuntimeException("Failed to load default configuration");
        }
        try {
            final var bufferedInputStream = new BufferedInputStream(resourceAsStream);
            return Buffer.buffer(bufferedInputStream.readAllBytes()).toJsonObject();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static DefaultProperties getDefaultProperties() {
        final var jsonConfig = getInstance().getJsonConfig();
        return PropertyUtil.getProperties(DefaultProperties.class, jsonConfig);
    }

    public static AliyunDnsProperties getAliyunDnsProperties() {
        final var jsonConfig = getInstance().getJsonConfig();
        return PropertyUtil.getProperties(AliyunDnsProperties.class, jsonConfig);
    }

    public static SecureConfig getDefaultSecureConfig() {
        final var jsonConfig = getInstance().getJsonConfig();
        return PropertyUtil.getProperties(SecureConfig.class, jsonConfig);
    }


    public JsonObject getJsonConfig() {
        return this.jsonConfig;
    }
}
