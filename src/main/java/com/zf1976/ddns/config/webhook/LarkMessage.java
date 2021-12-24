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

package com.zf1976.ddns.config.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zf1976.ddns.enums.WebhookProviderType;

import java.io.Serializable;

/**
 * @author mac
 * 2021/9/18 星期六 11:33 下午
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LarkMessage extends BaseMessage implements Serializable {

    public LarkMessage() {
        super(WebhookProviderType.LARK);
    }

    private String url;

    private String secret;

    @JsonProperty(value = "msg_type")
    private final String msgType = "text";

    private Content content;

    public String getMsgType() {
        return msgType;
    }

    public Content getContent() {
        return content;
    }

    public LarkMessage setContent(Content content) {
        this.content = content;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LarkMessage setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public LarkMessage setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public static LarkMessageBuilder newBuilder() {
        return new LarkMessageBuilder();
    }

    public static class Content  {
        private String text;

        public String getText() {
            return text;
        }

        public Content setText(String text) {
            this.text = text;
            return this;
        }
    }

    public static final class LarkMessageBuilder {
        private Boolean enabled = Boolean.FALSE;
        private String url;
        private String secret;
        private Content content;

        private LarkMessageBuilder() {
        }

        public LarkMessageBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public LarkMessageBuilder url(String url) {
            this.url = url;
            return this;
        }

        public LarkMessageBuilder secret(String secret) {
            this.secret = secret;
            return this;
        }

        public LarkMessageBuilder content(String content) {
            this.content = new Content().setText(content);
            return this;
        }

        public LarkMessage build() {
            LarkMessage larkMessage = new LarkMessage();
            larkMessage.setEnabled(enabled);
            larkMessage.setUrl(url);
            larkMessage.setSecret(secret);
            larkMessage.setContent(content);
            return larkMessage;
        }
    }
}
