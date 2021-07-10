package com.zf1976.ddns.property;

import com.zf1976.ddns.annotation.YamlPrefix;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/6
 */
@YamlPrefix(value = "aliyun")
public class AliyunDnsProperties {

    private String accessKeyId;

    private String secret;
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

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public AliyunDnsProperties setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public AliyunDnsProperties setSecret(String secret) {
        this.secret = secret;
        return this;
    }
}
