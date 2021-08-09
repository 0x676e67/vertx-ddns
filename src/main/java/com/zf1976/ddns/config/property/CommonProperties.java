package com.zf1976.ddns.config.property;

import com.zf1976.ddns.annotation.YamlPrefix;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/7
 */
@YamlPrefix(value = "common")
public class CommonProperties {

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

    public CommonProperties setIpApiList(List<String> ipApiList) {
        this.ipApiList = ipApiList;
        return this;
    }

    public List<String> getDnsServerList() {
        return dnsServerList;
    }

    public CommonProperties setDnsServerList(List<String> dnsServerList) {
        this.dnsServerList = dnsServerList;
        return this;
    }

    @Override
    public String toString() {
        return "CommonProperties{" +
                ", ipApiList=" + ipApiList +
                '}';
    }
}
