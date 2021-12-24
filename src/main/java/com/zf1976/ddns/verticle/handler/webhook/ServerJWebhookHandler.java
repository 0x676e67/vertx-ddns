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
