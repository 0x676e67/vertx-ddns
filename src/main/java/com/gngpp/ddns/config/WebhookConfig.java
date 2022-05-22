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

package com.gngpp.ddns.config;

import com.gngpp.ddns.config.webhook.DingDingMessage;
import com.gngpp.ddns.config.webhook.LarkMessage;
import com.gngpp.ddns.config.webhook.ServerJMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mac
 * 2021/8/23 星期一 8:23 上午
 */
public class WebhookConfig implements Serializable {

    private ServerJMessage serverJMessage;

    private List<DingDingMessage> dingDingMessageList;

    private LarkMessage larkMessage;

    public WebhookConfig() {
        this.dingDingMessageList = new ArrayList<>(3);
    }

    public ServerJMessage getServerJMessage() {
        return serverJMessage;
    }

    public LarkMessage getLarkMessage() {
        return larkMessage;
    }

    public WebhookConfig setLarkMessage(LarkMessage larkMessage) {
        this.larkMessage = larkMessage;
        return this;
    }

    public void setServerJMessage(ServerJMessage serverJMessage) {
        this.serverJMessage = serverJMessage;
    }

    public List<DingDingMessage> getDingDingMessageList() {
        return dingDingMessageList;
    }

    public void setDingDingMessageList(List<DingDingMessage> dingDingMessageList) {
        this.dingDingMessageList = dingDingMessageList;
    }

    @Override
    public String toString() {
        return "WebhookConfig{" +
                "serverJMessage=" + serverJMessage +
                ", dingDingMessageList=" + dingDingMessageList +
                ", larkMessage=" + larkMessage +
                '}';
    }
}
