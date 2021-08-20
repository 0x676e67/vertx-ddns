package com.zf1976.ddns.enums;

/**
 * @author mac
 * 2021/7/8
 */
public enum DnsProviderType {

    /**
     * 阿里云 0000
     */
    ALIYUN((byte) 0),
    /**
     * 腾讯云 0001
     */
    DNSPOD((byte) (1)),
    /**
     * Cloudflare 0010
     */
    CLOUDFLARE((byte) (1 << 1)),
    /**
     * 华为云 0100
     */
    HUAWEI((byte) (1 << 2));

    public final byte index;

    DnsProviderType(byte index) {
        this.index = index;
    }

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
