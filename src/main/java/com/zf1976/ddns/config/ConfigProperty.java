package com.zf1976.ddns.config;

import com.zf1976.ddns.util.PropertyUtil;
import io.vertx.core.json.JsonObject;
import org.yaml.snakeyaml.Yaml;

/**
 * @author mac
 * @date 2021/7/7
 */
public class ConfigProperty {

    private final JsonObject jsonConfig;
    private static ConfigProperty config;

    private ConfigProperty() {
        this.jsonConfig = loadJsonConfig();
    }

    public static ConfigProperty getInstance() {
       if (config == null) {
           synchronized (ConfigProperty.class) {
               if (config == null) {
                   config = new ConfigProperty();
               }
           }
       }
       return config;
    }

    private JsonObject loadJsonConfig() {
        final var jsonStr = new Yaml().load(PropertyUtil.class.getClassLoader()
                                                              .getResourceAsStream("config.yaml"));
        return JsonObject.mapFrom(jsonStr);
    }

    public JsonObject getJsonConfig() {
        return this.jsonConfig;
    }
}
