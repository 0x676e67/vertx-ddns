package com.zf1976.ddns.pojo;

import com.zf1976.ddns.verticle.DNSServiceType;

/**
 * @author mac
 * @date 2021/7/8
 */
public class DNSAccountDTO {

    /**
     * id 可能为空，某些DNS服务商只使用token
     */
    private String id;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 服务商类型
     */
    private DNSServiceType dnsServiceType;

    public String getId() {
        return id;
    }

    public DNSAccountDTO setId(String id) {
        this.id = id;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public DNSAccountDTO setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public DNSServiceType getDnsServiceType() {
        return dnsServiceType;
    }

    public DNSAccountDTO setDnsServiceType(DNSServiceType dnsServiceType) {
        this.dnsServiceType = dnsServiceType;
        return this;
    }

    @Override
    public String toString() {
        return "DNSAccountDTO{" +
                "id='" + id + '\'' +
                ", secret='" + secret + '\'' +
                ", dnsServiceType=" + dnsServiceType +
                '}';
    }
}
