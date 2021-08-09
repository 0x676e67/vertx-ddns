package com.zf1976.ddns.api.provider.exception;

/**
 * @author mac
 * 2021/8/9 星期一 10:58 下午
 */
public class NotSupportDnsProviderException extends RuntimeException{

    public NotSupportDnsProviderException(Throwable cause) {
        super(cause);
    }

    public NotSupportDnsProviderException(String message) {
        super(message);
    }

    public NotSupportDnsProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportDnsProviderException(String message,
                                          Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
