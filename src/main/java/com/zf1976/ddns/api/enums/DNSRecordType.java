package com.zf1976.ddns.api.enums;

/**
 * @author mac
 * @date 2021/7/18
 */
public enum DNSRecordType {
    /**
     * ipv4
     */
    A,
    /**
     * ipv6
     */
    AAAA;

    public static DNSRecordType checkType(String value) {
        for (DNSRecordType type : values()) {
            if (type.toString()
                    .equals(value)) {
                return type;
            }
        }
        throw new RuntimeException("The DNS Record Type provider does not exist");
    }
}
