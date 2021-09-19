package com.zf1976.ddns.verticle.handler.webhook;

import com.zf1976.ddns.api.signer.algorithm.Signer;
import com.zf1976.ddns.config.webhook.DingDingMessage;
import com.zf1976.ddns.util.ApiURLEncoderUtil;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.HttpResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;

/**
 * @author mac
 * 2021/8/26 星期四 9:57 下午
 */
public class DingDingWebhookHandler extends AbstractWebhookHandler<DingDingMessage> {


    public DingDingWebhookHandler() {
    }

    @Override
    public Future<HttpResponse<Buffer>> send(DingDingMessage dingDingMessage) {
        if (!dingDingMessage.getEnabled()) {
            return Future.succeededFuture();
        }
        final var signUrl = this.signUrl(dingDingMessage.getSecret(), dingDingMessage.getUrl());
        this.clearPrivacy(dingDingMessage);
        return super.client.postAbs(signUrl)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .sendBuffer(Json.encodeToBuffer(dingDingMessage));
    }

    private String signUrl(String secret, String url) {
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        var signData = Signer.getSHA256Signer()
                             .signString(stringToSign, secret);
        String sign = ApiURLEncoderUtil.encode(new String(Base64.encodeBase64(signData)));
        return url + "&timestamp=" + timestamp + "&sign=" + sign;
    }

    @Override
    protected void clearPrivacy(DingDingMessage dingDingMessage) {
        dingDingMessage.setSecret(null)
                .setUrl(null)
                .setEnabled(null)
                .setWebhookProviderType(null);
    }

}
