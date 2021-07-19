package com.zf1976.ddns.api.signature;

import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.util.DataTypeConverterUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class Signer {

    private final static Signer hmacSHA1Signer = new HmacSHA1Signer();
    private final static Signer hmacSHA256Signer = new HmacSHA256Signer();

    public static Signer getSHA1Signer() {
        return hmacSHA1Signer;
    }

    public static Signer getSHA256Signer() {
        return hmacSHA256Signer;
    }

    public abstract String signString(String stringToSign, DnsApiCredentials credentials);

    public abstract String signString(String stringToSign, String accessKeySecret);

    public abstract String getSignerName();

    public abstract String getSignerVersion();

    public abstract String getSignerType();

    protected static String sign(String stringToSign, String accessKeySecret, String algorithmName) {
        try {
            Mac mac = Mac.getInstance(algorithmName);
            mac.init(new SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), algorithmName));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return DataTypeConverterUtil._printBase64Binary(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException var5) {
            throw new IllegalArgumentException(var5.toString());
        }
    }
}
