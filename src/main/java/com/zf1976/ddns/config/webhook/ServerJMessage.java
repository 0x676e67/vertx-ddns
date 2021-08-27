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
