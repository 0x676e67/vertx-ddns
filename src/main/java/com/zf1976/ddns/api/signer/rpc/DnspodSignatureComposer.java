package com.zf1976.ddns.api.signer.rpc;

import com.zf1976.ddns.api.signer.algorithm.Signer;
import com.zf1976.ddns.enums.HttpMethod;
import com.zf1976.ddns.util.ApiURLEncoderUtil;
import com.zf1976.ddns.util.DataTypeConverterUtil;

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
