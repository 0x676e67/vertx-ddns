package com.zf1976.ddns.enums;

public enum HttpMethod {
    GET(false),
    PUT(true),
    POST(true),
    DELETE(false),
    HEAD(false),
    PATCH(true),
    OPTIONS(false);

    private final boolean hasContent;

    HttpMethod(boolean hasContent) {
        this.hasContent = hasContent;
    }

    public boolean hasContent() {
        return hasContent;
    }
}
