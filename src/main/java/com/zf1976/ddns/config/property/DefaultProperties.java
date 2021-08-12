package com.zf1976.ddns.config.property;

import com.zf1976.ddns.annotation.ConfigPrefix;

import java.util.List;

/**
 * @author mac
 * 2021/7/7
 */
@ConfigPrefix(value = "default")
public class DefaultProperties {

    /**
     * 默认用户名
     */
    private String defaultUsername;

    /**
     * 默认密码
     */
    private String defaultPassword;

    /**
     * ip Api
     */
    private List<String> ipApiList;

    /**
     * DNS Server List
     */
    private List<String> dnsServerList;

    public List<String> getIpApiList() {
        return ipApiList;
    }

    public DefaultProperties setIpApiList(List<String> ipApiList) {
        this.ipApiList = ipApiList;
        return this;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public DefaultProperties setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
        return this;
    }

    public DefaultProperties setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
        return this;
    }

    public List<String> getDnsServerList() {
        return dnsServerList;
    }

    public DefaultProperties setDnsServerList(List<String> dnsServerList) {
        this.dnsServerList = dnsServerList;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultProperties{" +
                "ipApiList=" + ipApiList +
                ", dnsServerList=" + dnsServerList +
                '}';
    }
}
