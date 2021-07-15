package com.zf1976.ddns.singer.aliyun.sign;

import com.zf1976.ddns.singer.aliyun.AliyunURLEncoder;
import com.zf1976.ddns.singer.aliyun.MethodType;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unused")
public class RpcSignatureComposer implements AliyunSignatureComposer {

    private final static String SEPARATOR = "&";
    private static AliyunSignatureComposer composer = null;
    private final Signer signer = Signer.getSigner();

    private RpcSignatureComposer() {

    }

    public static AliyunSignatureComposer getComposer() {
        if (null == composer) {
            synchronized (RpcSignatureComposer.class) {
                if (null == composer) {
                    composer = new RpcSignatureComposer();
                }
            }
        }
        return composer;
    }

    @Override
    public String composeStringToSign(MethodType method, Map<String, String> queryParamMap) {
        String[] sortedKeys = queryParamMap.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        StringBuilder canonicalizedQueryString = new StringBuilder();
        try {
            for (String key : sortedKeys) {
                canonicalizedQueryString.append("&")
                                        .append(AliyunURLEncoder.percentEncode(key))
                                        .append("=")
                                        .append(AliyunURLEncoder.percentEncode(queryParamMap.get(key)));
            }

            return method.toString() +
                    SEPARATOR +
                    AliyunURLEncoder.percentEncode("/") +
                    SEPARATOR +
                    AliyunURLEncoder.percentEncode(canonicalizedQueryString.substring(1));
        } catch (UnsupportedEncodingException exp) {
            throw new RuntimeException("UTF-8 encoding is not supported.");
        }

    }

    @Override
    public String toUrl(String accessKeySecret, String uriPattern, MethodType methodType, Map<String, String> queries) {
        // stringToSign
        final var stringToSign = this.composeStringToSign(methodType, queries);
        // 签名
        final var signature = this.signer.signString(stringToSign, accessKeySecret + "&");
        queries.put("Signature", signature);
        final var urlQueryParam = queries.keySet().toArray(new String[]{});
        Arrays.sort(urlQueryParam);
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (String key : urlQueryParam) {
            canonicalizedQueryString.append("&")
                                    .append(key)
                                    .append("=")
                                    .append(queries.get(key));
        }
        return uriPattern + "?" + canonicalizedQueryString.substring(1);
    }
}
