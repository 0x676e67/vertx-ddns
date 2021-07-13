package com.zf1976.ddns.verticle;

/**
 * @author mac
 * @date 2021/7/8
 */
public enum DNSServiceType {

    /**
     * 阿里云
     */
    ALIYUN,
    /**
     * 腾讯云
     */
    DNSPOD,
    /**
     * Cloudflare
     */
    CLOUDFLARE,
    /**
     * 华为云
     */
    HUAWEI;

    public static DNSServiceType checkType(String value) {
        for (DNSServiceType type : values()) {
            if (type.toString().equals(value)) {
                return type;
            }
        }
        return null;
    }
}