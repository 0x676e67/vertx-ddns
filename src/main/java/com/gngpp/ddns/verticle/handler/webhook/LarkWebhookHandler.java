/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
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

package com.gngpp.ddns.verticle.handler.webhook;

import com.gngpp.ddns.api.signer.algorithm.Signer;
import com.gngpp.ddns.config.webhook.LarkMessage;
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


    public LarkWebhookHandler() {

    }

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
