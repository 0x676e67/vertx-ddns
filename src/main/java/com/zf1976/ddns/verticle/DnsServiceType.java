package com.zf1976.ddns.verticle;

/**
 * @author mac
 * @date 2021/7/8
 */
public enum DnsServiceType {

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

    public static DnsServiceType checkType(String value) {
        for (DnsServiceType type : values()) {
            if (type.toString()
                    .equals(value)) {
                return type;
            }
        }
        throw new RuntimeException("The DDNS api provider does not exist");
    }

    public boolean check(DnsServiceType dnsServiceType) {
        return this == dnsServiceType;
    }
}
