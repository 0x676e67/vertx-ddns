package com.zf1976.ddns.api.signer.algorithm;

import com.zf1976.ddns.api.auth.DnsApiCredentials;

public class HmacSHA1Signer extends Signer {

    public static final String ALGORITHM_NAME = "HmacSHA1";

    public HmacSHA1Signer() {
    }

    public String signString(String stringToSign, String accessKeySecret) {
        return sign(stringToSign, accessKeySecret, ALGORITHM_NAME);
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
