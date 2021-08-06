package com.zf1976.ddns.api.signer.rpc;

import com.zf1976.ddns.api.enums.HttpMethod;
import com.zf1976.ddns.api.signer.algorithm.Signer;
import com.zf1976.ddns.util.ApiURLEncoderUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        try {
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
        } catch (UnsupportedEncodingException exp) {
            throw new RuntimeException("UTF-8 encoding is not supported.");
        }

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
        return canonicalizeRequestUrl(urlPattern, queries, URLEncoder.encode(signature, StandardCharsets.UTF_8));
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
