package com.zf1976.ddns.api.auth;

/**
 * @author mac
 * @date 2021/7/18
 */
public class TokenCredentials implements ProviderCredentials {

    private final String accessKeySecret;

    public TokenCredentials(String token) {

        if (token == null) {
            throw new IllegalArgumentException("Access key secret cannot be null.");
        }

        this.accessKeySecret = token;
    }


    @Override
    public String getAccessKeyId() {
        return null;
    }

    @Override
    public String getAccessKeySecret() {
        return this.accessKeySecret;
    }
}
