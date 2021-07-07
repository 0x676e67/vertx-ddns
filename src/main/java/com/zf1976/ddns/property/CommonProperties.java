package com.zf1976.ddns.property;

import com.zf1976.ddns.annotation.YamlPrefix;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/7
 */
@YamlPrefix(value = "common")
public class CommonProperties {

    List<String> ipApiList;

    public List<String> getIpApiList() {
        return ipApiList;
    }

    public CommonProperties setIpApiList(List<String> ipApiList) {
        this.ipApiList = ipApiList;
        return this;
    }

    @Override
    public String toString() {
        return "CommonProperties{" +
                "ipApiList=" + ipApiList +
                '}';
    }
}
