package com.zf1976.ddns.pojo;

import com.zf1976.ddns.verticle.DNSServiceType;

import java.io.Serializable;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/8
 */
public class DDNSConfig implements Serializable {

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
    /**
     * ipv4配置
     */
    private Ipv4Config ipv4Config;
    /**
     * ipv6配置
     */
    private Ipv6Config ipv6Config;

    public Ipv6Config getIpv6Config() {
        return ipv6Config;
    }

    public DDNSConfig setIpv6Config(Ipv6Config ipv6Config) {
        this.ipv6Config = ipv6Config;
        return this;
    }

    public Ipv4Config getIpv4Config() {
        return ipv4Config;
    }

    public DDNSConfig setIpv4Config(Ipv4Config ipv4Config) {
        this.ipv4Config = ipv4Config;
        return this;
    }

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

    public DNSServiceType getDnsServiceType() {
        return dnsServiceType;
    }

    public DDNSConfig setDnsServiceType(DNSServiceType dnsServiceType) {
        this.dnsServiceType = dnsServiceType;
        return this;
    }

    @Override
    public String toString() {
        return "DDNSConfig{" +
                "id='" + id + '\'' +
                ", secret='" + secret + '\'' +
                ", dnsServiceType=" + dnsServiceType +
                ", ipv4Config=" + ipv4Config +
                ", ipv6Config=" + ipv6Config +
                '}';
    }

    public static class Ipv4Config {
        private Boolean enable;
        private Boolean selectIpMethod;
        private String inputIpApi;
        private String networkIp;
        private List<String> domainList;

        public List<String> getDomainList() {
            return domainList;
        }

        public Ipv4Config setDomainList(List<String> domainList) {
            this.domainList = domainList;
            return this;
        }

        public String getInputIpApi() {
            return inputIpApi;
        }

        public Ipv4Config setInputIpApi(String inputIpApi) {
            this.inputIpApi = inputIpApi;
            return this;
        }

        public Boolean getEnable() {
            return enable;
        }

        public Ipv4Config setEnable(Boolean enable) {
            this.enable = enable;
            return this;
        }

        public Boolean getSelectIpMethod() {
            return selectIpMethod;
        }

        public Ipv4Config setSelectIpMethod(Boolean selectIpMethod) {
            this.selectIpMethod = selectIpMethod;
            return this;
        }

        public String getNetworkIp() {
            return networkIp;
        }

        public Ipv4Config setNetworkIp(String networkIp) {
            this.networkIp = networkIp;
            return this;
        }

        @Override
        public String toString() {
            return "Ipv4Config{" +
                    "enable=" + enable +
                    ", selectIpMethod=" + selectIpMethod +
                    ", inputIpApi='" + inputIpApi + '\'' +
                    ", networkIp='" + networkIp + '\'' +
                    ", domainList=" + domainList +
                    '}';
        }
    }

    public static class Ipv6Config{
        private Boolean enable;
        private Boolean selectIpMethod;
        private String inputIpApi;
        private String networkIp;
        private List<String> domainList;

        public Boolean getEnable() {
            return enable;
        }

        public Ipv6Config setEnable(Boolean enable) {
            this.enable = enable;
            return this;
        }

        public String getInputIpApi() {
            return inputIpApi;
        }

        public Ipv6Config setInputIpApi(String inputIpApi) {
            this.inputIpApi = inputIpApi;
            return this;
        }

        public Boolean getSelectIpMethod() {
            return selectIpMethod;
        }

        public Ipv6Config setSelectIpMethod(Boolean selectIpMethod) {
            this.selectIpMethod = selectIpMethod;
            return this;
        }

        public String getNetworkIp() {
            return networkIp;
        }

        public Ipv6Config setNetworkIp(String networkIp) {
            this.networkIp = networkIp;
            return this;
        }

        public List<String> getDomainList() {
            return domainList;
        }

        public Ipv6Config setDomainList(List<String> domainList) {
            this.domainList = domainList;
            return this;
        }

        @Override
        public String toString() {
            return "Ipv6Config{" +
                    "enable=" + enable +
                    ", selectIpMethod=" + selectIpMethod +
                    ", inputIpApi='" + inputIpApi + '\'' +
                    ", networkIp='" + networkIp + '\'' +
                    ", domainList=" + domainList +
                    '}';
        }
    }

}
