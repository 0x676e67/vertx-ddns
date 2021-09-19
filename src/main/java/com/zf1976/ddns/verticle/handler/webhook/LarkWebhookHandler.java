package com.zf1976.ddns.verticle.handler.webhook;

import com.zf1976.ddns.api.signer.algorithm.Signer;
import com.zf1976.ddns.config.webhook.LarkMessage;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.HttpResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;

/**
 * @author mac
 * 2021/9/18 星期六 11:33 下午
 */
public class LarkWebhookHandler extends AbstractWebhookHandler<LarkMessage> {

    @Override
    public Future<HttpResponse<Buffer>> send(LarkMessage larkMessage) {
        if (!larkMessage.getEnabled()) {
            return Future.succeededFuture();
        }
        final var url = larkMessage.getUrl();
        final var timeMillis = System.currentTimeMillis() / 1000;
        final var sign = this.genSign(larkMessage.getSecret(), timeMillis);
        this.clearPrivacy(larkMessage);
        final var buffer = Json.encodeToBuffer(larkMessage)
                .toJsonObject()
                .put("timestamp", timeMillis)
                .put("sign", sign).toBuffer();
        return super.client.postAbs(url)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .sendBuffer(buffer);
    }

    private String genSign(String secret, long timestamp) {
        var stringToSign = timestamp + "\n" + secret;
        var signData = Signer.getSHA256Signer().signString("", stringToSign);
        return new String(Base64.encodeBase64(signData));
    }

    @Override
    protected void clearPrivacy(LarkMessage larkMessage) {
        larkMessage.setUrl(null)
                .setSecret(null)
                .setEnabled(null)
                .setWebhookProviderType(null);
    }
}
