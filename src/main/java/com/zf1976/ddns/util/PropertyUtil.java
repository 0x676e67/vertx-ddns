package com.zf1976.ddns.util;

import com.zf1976.ddns.annotation.ConfigPrefix;
import io.vertx.core.json.JsonObject;

/**
 * @author mac
 * 2021/7/6
 */
public final class PropertyUtil {

    PropertyUtil() {

    }

    public static  <T> T getProperties(Class<T> tClass, JsonObject jsonConfig) {
        final var annotation = tClass.getAnnotation(ConfigPrefix.class);
        if (annotation != null) {
            return jsonConfig.getJsonObject(annotation.value())
                             .mapTo(tClass);
        }
        return null;
    }

}
