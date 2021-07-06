package com.zf1976.ddns.property;

import com.zf1976.ddns.annotation.YamlPrefix;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/6
 */
@YamlPrefix(value = "aliyun")
public class AliyunDnsProperties {

    /**
     * 地域id集合
     */
    List<String> regionIdList;

    public List<String> getRegionIdList() {
        return regionIdList;
    }

    public AliyunDnsProperties setRegionIdList(List<String> regionIdList) {
        this.regionIdList = regionIdList;
        return this;
    }

    @Override
    public String toString() {
        return "AliyunDnsProperties{" +
                "regionIdList=" + regionIdList +
                '}';
    }
}
