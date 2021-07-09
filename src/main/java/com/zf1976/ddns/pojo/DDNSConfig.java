package com.zf1976.ddns.pojo;

import com.zf1976.ddns.verticle.DDNSServiceType;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/8
 */
public class DDNSConfig {

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

    /**
     * 主域名
     */
    private List<String> domainList;


    public String getId() {
        return id;
    }

    public DDNSConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public DDNSConfig setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public DDNSServiceType getDnsServiceType() {
        return dnsServiceType;
    }

    public DDNSConfig setDnsServiceType(DDNSServiceType dnsServiceType) {
        this.dnsServiceType = dnsServiceType;
        return this;
    }

    public List<String> getDomainList() {
        return domainList;
    }

    public DDNSConfig setDomainList(List<String> domainList) {
        this.domainList = domainList;
        return this;
    }

    @Override
    public String toString() {
        return "DDNSConfig{" +
                "id='" + id + '\'' +
                ", secret='" + secret + '\'' +
                ", dnsServiceType=" + dnsServiceType +
                ", domainList=" + domainList +
                '}';
    }
}
