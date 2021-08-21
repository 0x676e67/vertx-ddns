package com.zf1976.ddns.api.signer.algorithm;

import com.zf1976.ddns.api.auth.ProviderCredentials;

public class HmacSHA1Signer extends Signer {

    public static final String ALGORITHM_NAME = "HmacSHA1";

    public HmacSHA1Signer() {
    }

    public byte[] signString(String stringToSign, String secret) {
        return sign(stringToSign, secret, ALGORITHM_NAME);
    }

    public byte[] signString(String stringToSign, ProviderCredentials credentials) {
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
