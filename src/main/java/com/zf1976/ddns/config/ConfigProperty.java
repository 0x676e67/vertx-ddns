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

package com.zf1976.ddns.config;

import com.zf1976.ddns.config.property.AliyunDnsProperties;
import com.zf1976.ddns.config.property.DefaultProperties;
import com.zf1976.ddns.util.PropertyUtil;
import io.vertx.core.buffer.Buffer;
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
