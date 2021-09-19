package com.zf1976.ddns.verticle.handler.webhook;

import com.zf1976.ddns.config.webhook.ServerJMessage;
import com.zf1976.ddns.util.ApiURLEncoderUtil;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;

public class ServerJWebhookHandler extends AbstractWebhookHandler<ServerJMessage> {

    public ServerJWebhookHandler() {
    }

    @Override
    public Future<HttpResponse<Buffer>> send(ServerJMessage serverJMessage) {
        if (!serverJMessage.getEnabled()) {
            return Future.succeededFuture();
        }
        final var encodeUrl = this.encodeUrl(serverJMessage.getUrl(), serverJMessage.getTitle(), serverJMessage.getContent());
        this.clearPrivacy(serverJMessage);
        return super.client.postAbs(encodeUrl).send();
    }

    private String encodeUrl(String url, String title, String content) {
        title = ApiURLEncoderUtil.encode(title);
        content = ApiURLEncoderUtil.encode(content);
        //noinspection SpellCheckingInspection
        return url + "?title=" + title + "&desp=" + content;
    }

    @Override
    protected void clearPrivacy(ServerJMessage serverJMessage) {
        serverJMessage.setUrl(null)
                .setEnabled(null)
                .setWebhookProviderType(null);
    }
}
