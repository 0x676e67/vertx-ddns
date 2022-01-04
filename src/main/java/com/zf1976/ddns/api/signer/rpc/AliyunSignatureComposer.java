/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
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

package com.zf1976.ddns.api.signer.rpc;

import com.zf1976.ddns.api.signer.algorithm.Signer;
import com.zf1976.ddns.enums.HttpMethod;
import com.zf1976.ddns.util.ApiURLEncoderUtil;
import com.zf1976.ddns.util.DataTypeConverterUtil;

import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unused")
public class AliyunSignatureComposer implements RpcAPISignatureComposer {

    private final static String SEPARATOR = "&";
    private final Signer signer = Signer.getSHA1Signer();

    private AliyunSignatureComposer() {

    }

    private static final class ComposerHolder {
        private static final RpcAPISignatureComposer composer = new AliyunSignatureComposer();
    }

    public static RpcAPISignatureComposer getComposer() {
        return ComposerHolder.composer;
    }

    @Override
    public String composeStringToSign(HttpMethod method, Map<String, Object> queryParamMap) {
        String[] sortedKeys = queryParamMap.keySet()
                                           .toArray(new String[]{});
        Arrays.sort(sortedKeys);
        StringBuilder canonicalizeQueryString = new StringBuilder();
        for (String key : sortedKeys) {
            canonicalizeQueryString.append("&")
                                   .append(ApiURLEncoderUtil.aliyunPercentEncode(key))
                                   .append("=")
                                   .append(ApiURLEncoderUtil.aliyunPercentEncode(queryParamMap.get(key)
                                                                                              .toString()));
        }

        return method.name() +
                SEPARATOR +
                ApiURLEncoderUtil.aliyunPercentEncode("/") +
                SEPARATOR +
                ApiURLEncoderUtil.aliyunPercentEncode(canonicalizeQueryString.substring(1));

    }

    @Override
    public String toSignatureUrl(String accessKeySecret,
                                 String urlPattern,
                                 HttpMethod methodType,
                                 Map<String, Object> queries) {
        // stringToSign
        final var stringToSign = this.composeStringToSign(methodType, queries);
        // 签名
        final var signature = this.signer.signString(stringToSign, accessKeySecret);
        final var signatureBase64 = DataTypeConverterUtil._printBase64Binary(signature);
        return canonicalizeRequestUrl(urlPattern, queries, ApiURLEncoderUtil.aliyunPercentEncode(signatureBase64));
    }


    @Override
    public String signatureMethod() {
        return this.signer.getSignerName();
    }

    @Override
    public String getSignerVersion() {
        return this.signer.getSignerVersion();
    }
}
