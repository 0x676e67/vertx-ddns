package com.zf1976.ddns.api.singer.aliyun.sign;

import com.zf1976.ddns.api.singer.aliyun.auth.AlibabaCloudCredentials;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class Signer {

    private final static Signer hmacSHA1Signer = new HmacSHA1Signer();

    public static Signer getSigner() {
        return hmacSHA1Signer;
    }

    public abstract String signString(String stringToSign, AlibabaCloudCredentials credentials);

    public abstract String signString(String stringToSign, String accessKeySecret);

    public abstract String getSignerName();

    public abstract String getSignerVersion();

    public abstract String getSignerType();
}
