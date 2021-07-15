package com.zf1976.ddns.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.zf1976.ddns.singer.aliyun.auth.AlibabaCloudCredentials;
import com.zf1976.ddns.singer.aliyun.auth.BasicCredentials;

/**
 * @author mac
 * @date 2021/7/14
 */
@SuppressWarnings("FieldCanBeLocal")
public class AliyunDnsService {

    private final AlibabaCloudCredentials credentials;
    private final ObjectMapper objectMapper;
    public AliyunDnsService(String accessKeyId, String accessKeySecret) {
        this(new BasicCredentials(accessKeyId, accessKeySecret));
    }

    public AliyunDnsService(AlibabaCloudCredentials credentials) {
        this.credentials = credentials;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

}
