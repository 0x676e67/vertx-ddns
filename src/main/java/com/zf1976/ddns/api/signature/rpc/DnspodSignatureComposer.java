package com.zf1976.ddns.api.signature.rpc;

import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signature.Signer;

import java.util.Arrays;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/19
 */
public class DnspodSignatureComposer implements RpcAPISignatureComposer {

    private final static String SEPARATOR = "&";
    private static RpcAPISignatureComposer composer = null;
    private final Signer signer = Signer.getSHA256Signer();

    private DnspodSignatureComposer() {

    }

    public static RpcAPISignatureComposer getComposer() {
        if (null == composer) {
            synchronized (AliyunSignatureComposer.class) {
                if (null == composer) {
                    composer = new DnspodSignatureComposer();
                }
            }
        }
        return composer;
    }

    @Override
    public String composeStringToSign(MethodType method, Map<String, Object> queries) {
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
    public String toUrl(String accessKeySecret, String urlPattern, MethodType methodType, Map<String, Object> queries) {
        final var stringToSign = this.composeStringToSign(methodType, queries);
        final var signature = this.signer.signString(stringToSign, accessKeySecret);
        return getUrl(urlPattern, queries, signature);
    }
}
