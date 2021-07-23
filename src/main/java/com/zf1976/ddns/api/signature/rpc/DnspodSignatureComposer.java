package com.zf1976.ddns.api.signature.rpc;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signature.Signer;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/19
 */
public class DnspodSignatureComposer implements RpcAPISignatureComposer {

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
    public String composeStringToSign(HttpRequest request, Map<String, Object> queries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toSignatureUrl(String accessKeySecret,
                                 String urlPattern,
                                 MethodType methodType,
                                 Map<String, Object> queries) {
        final var stringToSign = this.composeStringToSign(methodType, queries);
        final var signature = this.signer.signString(stringToSign, accessKeySecret);
        // 这里需要给签名做URL编码，否则会连续请求会间歇性认证失败
        return getUrl(urlPattern, queries, URLEncoder.encode(signature, StandardCharsets.UTF_8));
    }

    @Override
    public void signRequest(BasicCredentials basicCredentials, HttpRequest request, Map<String, Object> queries) {
        throw new UnsupportedOperationException();
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
