package com.zf1976.ddns.config.webhook;

import com.zf1976.ddns.enums.WebhookProviderType;

/**
 * @author mac
 * 2021/8/22 星期日 11:53 下午
 */
public class BaseMessage {

    protected WebhookProviderType webhookProviderType;

    protected Boolean enabled = Boolean.FALSE;

    protected BaseMessage(WebhookProviderType webhookProviderType) {
        this.webhookProviderType = webhookProviderType;
    }

    public WebhookProviderType getWebhookProviderType() {
        return webhookProviderType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public BaseMessage setWebhookProviderType(WebhookProviderType webhookProviderType) {
        this.webhookProviderType = webhookProviderType;
        return this;
    }

    public BaseMessage setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
