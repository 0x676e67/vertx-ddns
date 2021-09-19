package com.zf1976.ddns.config;

import com.zf1976.ddns.config.webhook.DingDingMessage;
import com.zf1976.ddns.config.webhook.LarkMessage;
import com.zf1976.ddns.config.webhook.ServerJMessage;

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
