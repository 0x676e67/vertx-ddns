package com.zf1976.ddns.api.signer.algorithm;

import com.zf1976.ddns.api.auth.ProviderCredentials;

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

    protected static byte[] sign(String stringToSign, String secret, String algorithmName) {
        try {
            Mac mac = Mac.getInstance(algorithmName);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithmName));
            return mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException var5) {
            throw new IllegalArgumentException(var5.toString());
        }
    }

    public abstract byte[] signString(String stringToSign, ProviderCredentials credentials);

    public abstract String getSignerName();

    public abstract String getSignerVersion();

    public abstract String getSignerType();

    public abstract byte[] signString(String stringToSign, String secret);
}
