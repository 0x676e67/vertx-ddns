package com.zf1976.ddns.verticle.handler.webhook;

import com.zf1976.ddns.verticle.handler.spi.WebhookHandler;
import io.vertx.ext.web.client.WebClient;

/**
 * @author mac
 * 2021/9/18 星期六 11:42 下午
 */
public abstract class AbstractWebhookHandler<T> implements WebhookHandler<T> {

    protected WebClient client;

    @Override
    public void initClient(WebClient client) {
        this.client = client;
    }

    protected void clearPrivacy(T t) {

    }
}
