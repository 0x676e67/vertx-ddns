package com.zf1976.ddns.api.singer.aliyun.sign;

import com.zf1976.ddns.api.singer.aliyun.MethodType;

import java.util.Map;

public interface AliyunSignatureComposer {

    String composeStringToSign(MethodType method, Map<String, String> queries);

    String toUrl(String accessKeySecret, String url,MethodType methodType,  Map<String, String> queries);
}
