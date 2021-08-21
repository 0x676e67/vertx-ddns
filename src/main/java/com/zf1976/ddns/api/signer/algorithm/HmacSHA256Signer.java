package com.zf1976.ddns.api.signer.algorithm;

import com.zf1976.ddns.api.auth.ProviderCredentials;

/**
 * @author mac
 * 2021/7/19
 */
public class HmacSHA256Signer extends Signer {

    public static final String ALGORITHM_NAME = "HmacSHA256";

    @Override
    public byte[] signString(String stringToSign, ProviderCredentials credentials) {
        return signString(stringToSign, credentials.getAccessKeySecret());
    }

    @Override
    public byte[] signString(String stringToSign, String secret) {
        return sign(stringToSign, secret, ALGORITHM_NAME);
    }

    public String getSignerName() {
        return "HMAC-SHA256";
    }

    public String getSignerVersion() {
        return "1.0";
    }

    @Override
    public String getSignerType() {
        return null;
    }
}
