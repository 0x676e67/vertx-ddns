package com.zf1976.ddns.pojo;

import com.zf1976.ddns.verticle.DDNSServiceType;

/**
 * @author mac
 * @date 2021/7/8
 */
public class DNSAccount {

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
    private DDNSServiceType dnsServiceType;

    public String getId() {
        return id;
    }

    public DNSAccount setId(String id) {
        this.id = id;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public DNSAccount setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public DDNSServiceType getDnsServiceType() {
        return dnsServiceType;
    }

    public DNSAccount setDnsServiceType(DDNSServiceType dnsServiceType) {
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
