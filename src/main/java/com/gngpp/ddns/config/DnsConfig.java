/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.config;

import com.gngpp.ddns.enums.DnsProviderType;

import java.io.Serializable;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/8
 */
public class DnsConfig implements Serializable {

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
    private DnsProviderType dnsProviderType;
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

    public DnsConfig setIpv6Config(Ipv6Config ipv6Config) {
        this.ipv6Config = ipv6Config;
        return this;
    }

    public Ipv4Config getIpv4Config() {
        return ipv4Config;
    }

    public DnsConfig setIpv4Config(Ipv4Config ipv4Config) {
        this.ipv4Config = ipv4Config;
        return this;
    }

    public String getId() {
        return id;
    }

    public DnsConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public DnsConfig setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public DnsProviderType getDnsProviderType() {
        return dnsProviderType;
    }

    public DnsConfig setDnsProviderType(DnsProviderType dnsProviderType) {
        this.dnsProviderType = dnsProviderType;
        return this;
    }

    @Override
    public String toString() {
        return "DnsConfig{" +
                "id='" + id + '\'' +
                ", secret='" + secret + '\'' +
                ", dnsProviderType=" + dnsProviderType +
                ", ipv4Config=" + ipv4Config +
                ", ipv6Config=" + ipv6Config +
                '}';
    }

    public static class Ipv4Config {
        /**
         * 开启ipv4记录解析
         */
        private Boolean enable = Boolean.FALSE;
        /**
         * true:使用ip API获取本机ip
         * false:使用网卡获取本机ip
         */
        private Boolean selectIpMethod = Boolean.TRUE;
        /**
         * ip API
         */
        private String inputIpApi;
        /**
         * 网卡
         */
        private String card;
        /**
         * 记录解析列表
         */
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

        public String getCard() {
            return card;
        }

        public Ipv4Config setCard(String card) {
            this.card = card;
            return this;
        }

        @Override
        public String toString() {
            return "Ipv4Config{" +
                    "enable=" + enable +
                    ", selectIpMethod=" + selectIpMethod +
                    ", inputIpApi='" + inputIpApi + '\'' +
                    ", card='" + card + '\'' +
                    ", domainList=" + domainList +
                    '}';
        }
    }

    public static class Ipv6Config{
        /**
         * 开启ipv4记录解析
         */
        private Boolean enable = Boolean.FALSE;
        /**
         * true:使用ip API获取本机ip
         * false:使用网卡获取本机ip
         */
        private Boolean selectIpMethod = Boolean.TRUE;
        /**
         * ip API
         */
        private String inputIpApi;
        /**
         * 网卡
         */
        private String card;
        /**
         * 记录解析列表
         */
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

        public String getCard() {
            return card;
        }

        public Ipv6Config setCard(String card) {
            this.card = card;
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
                    ", card='" + card + '\'' +
                    ", domainList=" + domainList +
                    '}';
        }
    }

}
