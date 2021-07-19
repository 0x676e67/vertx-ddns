package com.zf1976.ddns.api.signature.rpc;

import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signature.Signer;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

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

    public static void main(String[] args) {
        TreeMap<String, Object> params = new TreeMap<>(); // TreeMap可以自动排序
        params.put("Nonce", new Random().nextInt(java.lang.Integer.MAX_VALUE)); // 公共参数
        // 实际调用时应当使用系统当前时间，例如：   params.put("Timestamp", System.currentTimeMillis() / 1000);
        params.put("Timestamp", String.valueOf(System.currentTimeMillis() / 1000)); // 公共参数
        params.put("SecretId", "AKID4wYtxkHXWDeohJeiUzHI2VhcGdjZ8QRD"); // 公共参数
        params.put("Action", "DescribeInstances");
        params.put("Version", "2017-03-12");
        params.put("SignatureMethod", "HmacSHA256");
        final var composer = getComposer();
        final var url = composer.toUrl("WljQqzrRloZ3HEuJQHr9AvOaU8VaOuA9", "https://cvm.tencentcloudapi.com", MethodType.GET, params);
        System.out.println(url);
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
