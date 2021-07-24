package com.zf1976.ddns.api.signer.rpc;

import com.zf1976.ddns.api.enums.MethodType;

import java.util.Map;

public interface RpcAPISignatureComposer {

    String composeStringToSign(MethodType method, Map<String, Object> queries);

    String toSignatureUrl(String accessKeySecret,
                          String urlPattern,
                          MethodType methodType,
                          Map<String, Object> queries);


    default String getUrl(String urlPattern, Map<String, Object> queries, String signature) {
        queries.put("Signature", signature);
        StringBuilder canonicalizeQueryString = new StringBuilder();
        for (String key : queries.keySet()) {
            canonicalizeQueryString.append("&")
                                   .append(key)
                                   .append("=")
                                   .append(queries.get(key));
        }
        return urlPattern + "?" + canonicalizeQueryString.substring(1);
    }

    String signatureMethod();

    String getSignerVersion();
}
