package com.zf1976.ddns.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author mac
 * 2021/8/22 星期日 2:26 下午
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerJMessage {

    private String title;

    private String content;

    private String url;

    public ServerJMessage() {

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
}
