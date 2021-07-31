package com.zf1976.ddns.config;

import com.zf1976.ddns.property.AliyunDnsProperties;
import com.zf1976.ddns.property.CommonProperties;
import com.zf1976.ddns.util.PropertyUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.BufferedInputStream;

/**
 * @author mac
 * @date 2021/7/7
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
            final var decodeValue = Json.decodeValue(Buffer.buffer(bufferedInputStream.readAllBytes()));
            return JsonObject.mapFrom(decodeValue);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static CommonProperties getCommonProperties() {
        return PropertyUtil.getProperties(CommonProperties.class, getInstance().getJsonConfig());
    }

    public static AliyunDnsProperties getAliyunDnsProperties() {
        return PropertyUtil.getProperties(AliyunDnsProperties.class, getInstance().getJsonConfig());
    }


    public JsonObject getJsonConfig() {
        return this.jsonConfig;
    }
}
