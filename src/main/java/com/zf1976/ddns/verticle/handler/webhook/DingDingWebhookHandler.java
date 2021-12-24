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
