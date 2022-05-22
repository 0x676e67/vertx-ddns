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

import com.gngpp.ddns.api.signer.algorithm.Signer;
import com.gngpp.ddns.util.ApiURLEncoderUtil;
import com.gngpp.ddns.enums.HttpMethod;
import com.gngpp.ddns.util.DataTypeConverterUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * @author mac
 * 2021/7/19
 */
public class DnspodSignatureComposer implements RpcAPISignatureComposer {

    private final Signer signer = Signer.getSHA256Signer();

    private DnspodSignatureComposer() {

    }

    private static final class ComposerHolder {
        private static final RpcAPISignatureComposer composer = new DnspodSignatureComposer();
    }

    public static RpcAPISignatureComposer getComposer() {
        return ComposerHolder.composer;
    }

    @Override
    public String composeStringToSign(HttpMethod method, Map<String, Object> queries) {
        String[] sortedKeys = queries.keySet()
                                     .toArray(new String[]{});
        Arrays.sort(sortedKeys);
        @SuppressWarnings("SpellCheckingInspection")
        StringBuilder canonicalizedQueryString = new StringBuilder(method.name() + "dnspod.tencentcloudapi.com/?");
        for (String key : sortedKeys) {
            canonicalizedQueryString.append(key)
                                    .append("=")
                                    .append(queries.get(key))
                                    .append("&");
        }
        return canonicalizedQueryString.substring(0, canonicalizedQueryString.length() - 1);
    }


    @Override
    public String toSignatureUrl(String accessKeySecret,
                                 String urlPattern,
                                 HttpMethod methodType,
                                 Map<String, Object> queries) {
        final var stringToSign = this.composeStringToSign(methodType, queries);
        final var signature = this.signer.signString(stringToSign, accessKeySecret);
        final var base64Binary = DataTypeConverterUtil._printBase64Binary(signature);
        // 这里需要给签名做URL编码，否则会连续请求会间歇性认证失败
        return canonicalizeRequestUrl(urlPattern, queries, ApiURLEncoderUtil.encode(base64Binary));
    }

    @Override
    public String signatureMethod() {
        return "HmacSHA256";
    }

    @Override
    public String getSignerVersion() {
        return this.signer.getSignerVersion();
    }
}
