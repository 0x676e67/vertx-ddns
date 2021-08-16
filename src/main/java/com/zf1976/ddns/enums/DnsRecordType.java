package com.zf1976.ddns.enums;

/**
 * Represents a DNS record type.
 * @author mac
 * 2021/7/18
 */
public enum DnsRecordType {
    /**
     * ipv4
     */
    A,
    /**
     * ipv6
     */
    AAAA;

    public static DnsRecordType checkType(String value) {
        for (DnsRecordType type : values()) {
            if (type.toString()
                    .equals(value)) {
                return type;
            }
        }
        throw new RuntimeException("The DNS Record Type provider does not exist");
    }
}
