package com.zf1976.ddns.api.signature.aliyun.sign;

import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.util.DataTypeConverterUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacSHA1Signer extends Signer {

    private static final String ALGORITHM_NAME = "HmacSHA1";

    public HmacSHA1Signer() {
    }

    public String signString(String stringToSign, String accessKeySecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), ALGORITHM_NAME));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return DataTypeConverterUtil._printBase64Binary(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException var5) {
            throw new IllegalArgumentException(var5.toString());
        }
    }

    public String signString(String stringToSign, DnsApiCredentials credentials) {
        return this.signString(stringToSign, credentials.getAccessKeySecret());
    }

    public String getSignerName() {
        return "HMAC-SHA1";
    }

    public String getSignerVersion() {
        return "1.0";
    }

    public String getSignerType() {
        return null;
    }
}
