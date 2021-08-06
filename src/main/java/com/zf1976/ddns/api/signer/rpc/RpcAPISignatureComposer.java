package com.zf1976.ddns.api.signer.rpc;

import com.zf1976.ddns.api.enums.HttpMethod;

import java.util.Map;

/**
 * 基于URL签名的组合接口
 */
public interface RpcAPISignatureComposer {

    /**
     * 组合签名必要参数
     *
     * @param method  请求方法
     * @param queries 请求参数
     * @return {@link String}
     */
    String composeStringToSign(HttpMethod method, Map<String, Object> queries);

    /**
     * 生成经过签名的URL
     *
     * @param accessKeySecret 服务商密钥
     * @param urlPattern      url
     * @param methodType      请求方法
     * @param queries         请求参数
     * @return {@link String}
     */
    String toSignatureUrl(String accessKeySecret,
                          String urlPattern,
                          HttpMethod methodType,
                          Map<String, Object> queries);

    /**
     * 签名方法
     *
     * @return {@link String}
     */
    String signatureMethod();

    /**
     * 签名版本
     *
     * @return {@link String}
     */
    String getSignerVersion();

    /**
     * 规范请求URL
     *
     * @param urlPattern url
     * @param queries    请求参数
     * @param signature  签名
     * @return {@link String}
     */
    default String canonicalizeRequestUrl(String urlPattern, Map<String, Object> queries, String signature) {
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
}
