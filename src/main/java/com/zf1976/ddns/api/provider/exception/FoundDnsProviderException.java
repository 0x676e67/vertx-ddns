package com.zf1976.ddns.api.provider.exception;

/**
 * @author mac
 * @date 2021/8/9
 */
public class FoundDnsProviderException extends RuntimeException{

    public FoundDnsProviderException(Throwable cause) {
        super(cause);
    }

    public FoundDnsProviderException(String message) {
        super(message);
    }

    public FoundDnsProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public FoundDnsProviderException(String message,
                                     Throwable cause,
                                     boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
