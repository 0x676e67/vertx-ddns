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
import com.zf1976.ddns.enums.WebhookProviderType;

import java.io.Serializable;

/**
 * @author mac
 * 2021/8/22 星期日 2:26 下午
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerJMessage extends BaseMessage implements Serializable {

    private String url;

    private String title;

    private String content;

    public ServerJMessage() {
        super(WebhookProviderType.SERVER_J);
    }

    public ServerJMessage(ServerJMessageBuilder builder) {
        this();
        this.url = builder.url;
        this.title = builder.title;
        this.content = builder.content;
        this.enabled = builder.enabled;

    }

    public String getTitle() {
        return title;
    }

    public ServerJMessage setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ServerJMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ServerJMessage setUrl(String url) {
        this.url = url;
        return this;
    }

    public static ServerJMessageBuilder newBuilder() {
        return new ServerJMessageBuilder();
    }

    @Override
    public String toString() {
        return "ServerJMessage{" +
                "url='" + url + '\'' +
                ", webhookProviderType=" + webhookProviderType +
                ", enabled=" + enabled +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    public static final class ServerJMessageBuilder {
        private Boolean enabled = Boolean.FALSE;
        private String url;
        private String title;
        private String content;

        private ServerJMessageBuilder() {
        }


        public ServerJMessageBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ServerJMessageBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ServerJMessageBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ServerJMessageBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ServerJMessage build() {
            ServerJMessage serverJMessage = new ServerJMessage();
            serverJMessage.setEnabled(enabled);
            serverJMessage.setUrl(url);
            serverJMessage.setTitle(title);
            serverJMessage.setContent(content);
            return serverJMessage;
        }
    }
}
