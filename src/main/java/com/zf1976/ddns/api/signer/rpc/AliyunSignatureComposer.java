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
