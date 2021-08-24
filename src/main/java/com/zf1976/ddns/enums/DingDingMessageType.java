package com.zf1976.ddns.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author mac
 * 2021/8/21 星期六 1:17 下午
 */
public enum DingDingMessageType {

    /**
     * text类型
     */
    TEXT("text"),
    /**
     * markdown类型
     */
    MARKDOWN("markdown"),
    /**
     * link类型
     */
    LINK("link");

    public final String value;

    DingDingMessageType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return this.value;
    }

}
