package com.zf1976.ddns.config.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zf1976.ddns.annotation.ConfigPrefix;

import java.util.List;

/**
 * @author mac
 *2021/7/6
 */
@ConfigPrefix(value = "aliyun")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AliyunDnsProperties {

    /**
     * 默认地域id
     */
    private String defaultRegionId;
    /**
     * 地域id集合
     */
    private List<String> regionIdList;

    public List<String> getRegionIdList() {
        return regionIdList;
    }

    public AliyunDnsProperties setRegionIdList(List<String> regionIdList) {
        this.regionIdList = regionIdList;
        return this;
    }

    public String getDefaultRegionId() {
        return defaultRegionId;
    }

    public AliyunDnsProperties setDefaultRegionId(String defaultRegionId) {
        this.defaultRegionId = defaultRegionId;
        return this;
    }

    @Override
    public String toString() {
        return "AliyunDnsProperties{" +
                "defaultRegionId='" + defaultRegionId + '\'' +
                ", regionIdList=" + regionIdList +
                '}';
    }
}
