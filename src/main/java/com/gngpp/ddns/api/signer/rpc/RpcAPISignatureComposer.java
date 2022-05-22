/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.api.signer.rpc;

import com.gngpp.ddns.enums.HttpMethod;

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
