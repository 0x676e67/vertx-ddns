package com.zf1976.ddns.api.singer.aliyun;

public enum MethodType {
    GET(false),
    PUT(true),
    POST(true),
    DELETE(false),
    HEAD(false),
    OPTIONS(false);

    private final boolean hasContent;

    MethodType(boolean hasContent) {
        this.hasContent = hasContent;
    }

    public boolean hasContent() {
        return hasContent;
    }
}
