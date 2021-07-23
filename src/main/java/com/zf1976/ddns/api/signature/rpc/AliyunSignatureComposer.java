package com.zf1976.ddns.api.signature.rpc;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signature.Signer;
import com.zf1976.ddns.util.ApiURLEncoder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unused")
public class AliyunSignatureComposer implements RpcAPISignatureComposer {

    private final static String SEPARATOR = "&";
    private static RpcAPISignatureComposer composer = null;
    private final Signer signer = Signer.getSHA1Signer();

    private AliyunSignatureComposer() {

    }

    public static RpcAPISignatureComposer getComposer() {
        if (null == composer) {
            synchronized (AliyunSignatureComposer.class) {
                if (null == composer) {
                    composer = new AliyunSignatureComposer();
                }
            }
        }
        return composer;
    }

    @Override
    public String composeStringToSign(MethodType method, Map<String, Object> queryParamMap) {
        String[] sortedKeys = queryParamMap.keySet()
                                           .toArray(new String[]{});
        Arrays.sort(sortedKeys);
        StringBuilder canonicalizedQueryString = new StringBuilder();
        try {
            for (String key : sortedKeys) {
                canonicalizedQueryString.append("&")
                                        .append(ApiURLEncoder.aliyunPercentEncode(key))
                                        .append("=")
                                        .append(ApiURLEncoder.aliyunPercentEncode(queryParamMap.get(key)
                                                                                               .toString()));
            }

            return method.name() +
                    SEPARATOR +
                    ApiURLEncoder.aliyunPercentEncode("/") +
                    SEPARATOR +
                    ApiURLEncoder.aliyunPercentEncode(canonicalizedQueryString.substring(1));
        } catch (UnsupportedEncodingException exp) {
            throw new RuntimeException("UTF-8 encoding is not supported.");
        }

    }

    @Override
    public String composeStringToSign(HttpRequest request, Map<String, Object> queries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toSignatureUrl(String accessKeySecret,
                                 String urlPattern,
                                 MethodType methodType,
                                 Map<String, Object> queries) {
        // stringToSign
        final var stringToSign = this.composeStringToSign(methodType, queries);
        // 签名
        final var signature = this.signer.signString(stringToSign, accessKeySecret);
        return getUrl(urlPattern, queries, URLEncoder.encode(signature, StandardCharsets.UTF_8));
    }

    @Override
    public void signRequest(BasicCredentials basicCredentials, HttpRequest request, Map<String, Object> queries) {
        throw new UnsupportedOperationException();
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
