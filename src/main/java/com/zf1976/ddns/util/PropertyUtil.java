package com.zf1976.ddns.util;

import com.zf1976.ddns.annotation.YamlPrefix;
import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.property.AliyunDnsProperties;
import com.zf1976.ddns.property.CommonProperties;
import io.vertx.core.json.JsonObject;
import org.yaml.snakeyaml.Yaml;

/**
 * @author mac
 * @date 2021/7/6
 */
public final class PropertyUtil {

    PropertyUtil() {

    }

    public static  <T> T getProperties(Class<T> tClass, JsonObject jsonConfig) {
        final var annotation = tClass.getAnnotation(YamlPrefix.class);
        if (annotation != null) {
            return jsonConfig.getJsonObject(annotation.value())
                             .mapTo(tClass);
        }
        return null;
    }

}
