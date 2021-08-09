package com.zf1976.ddns.api.enums;

/**
 * @author mac
 * @date 2021/7/8
 */
public enum DnsProviderType {

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

    public static DnsProviderType checkType(String value) {
        for (DnsProviderType type : values()) {
            if (type.toString()
                    .equals(value)) {
                return type;
            }
        }
        throw new RuntimeException("The DDNS api provider does not exist");
    }

    public boolean check(DnsProviderType dnsServiceType) {
        return this == dnsServiceType;
    }
}
