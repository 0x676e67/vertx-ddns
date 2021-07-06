package com.zf1976.ddns.api;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.zf1976.ddns.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/6
 */
public class AliyunDDNSApiService {

    private static final Logger log = LoggerFactory.getLogger("[AliyunDDNSApi]");
    /**
     * 获取主域名的所有解析记录列表
     */
    public DescribeDomainRecordsResponse describeDomainRecords(DescribeDomainRecordsRequest request, IAcsClient client){
        try {
            // 调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            log.error(e.getMessage(), e.getCause());
            // 发生调用错误，抛出运行时异常
            throw new RuntimeException(e);
        }
    }


    /**
     * 修改解析记录
     */
    public UpdateDomainRecordResponse updateDomainRecord(UpdateDomainRecordRequest request, IAcsClient client){
        try {
            // 调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            log.error(e.getMessage(), e.getCause());
            // 发生调用错误，抛出运行时异常
            throw new RuntimeException(e);
        }
    }

}
