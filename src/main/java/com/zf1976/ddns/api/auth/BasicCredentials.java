package com.zf1976.ddns.api.auth;

public record BasicCredentials(String accessKeyId,
                               String accessKeySecret) implements ProviderCredentials {

    public BasicCredentials {
        if (accessKeyId == null) {
            throw new IllegalArgumentException("Access key ID cannot be null.");
        }
        if (accessKeySecret == null) {
            throw new IllegalArgumentException("Access key secret cannot be null.");
        }

    }

    @Override
    public String getAccessKeyId() {
        return accessKeyId;
    }

    @Override
    public String getAccessKeySecret() {
        return accessKeySecret;
    }

}
