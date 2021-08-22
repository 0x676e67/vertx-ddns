package com.zf1976.ddns.enums;

/**
 * @author mac
 * 2021/8/22 星期日 2:26 下午
 */
public enum WebhookProviderType {

    /**
     * Server酱
     */
    SERVER_J,
    /**
     * DingDing（钉钉）
     */
    DING_DING;

    public static WebhookProviderType checkType(String value) {
        for (WebhookProviderType type : values()) {
            if (type.toString()
                    .equals(value)) {
                return type;
            }
        }
        throw new RuntimeException("The Webhook Type provider does not exist");
    }
}
