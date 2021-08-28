package com.zf1976.ddns.verticle.handler.webhook;

import com.zf1976.ddns.config.webhook.ServerJMessage;
import com.zf1976.ddns.util.ApiURLEncoderUtil;
import com.zf1976.ddns.verticle.handler.spi.WebhookHandler;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class ServerJWebhookHandler implements WebhookHandler<ServerJMessage> {

    private WebClient webClient;

    public ServerJWebhookHandler() {
    }

    public ServerJWebhookHandler(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Future<HttpResponse<Buffer>> send(ServerJMessage serverJMessage) {
        if (!serverJMessage.getEnabled()) {
            return Future.succeededFuture();
        }
        final var encodeUrl = this.encodeUrl(serverJMessage.getUrl(), serverJMessage.getTitle(), serverJMessage.getContent());
        return this.webClient.postAbs(encodeUrl).send();
    }

    @Override
    public void initClient(WebClient client) {
        this.webClient = client;
    }

    private String encodeUrl(String url, String title, String content) {
        title = ApiURLEncoderUtil.encode(title);
        content = ApiURLEncoderUtil.encode(content);
        //noinspection SpellCheckingInspection
        return url + "?title=" + title + "&desp=" + content;
    }

}
