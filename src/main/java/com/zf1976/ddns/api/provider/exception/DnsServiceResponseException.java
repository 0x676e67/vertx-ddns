package com.zf1976.ddns.api.provider.exception;

/**
 * @author mac
 * @date 2021/8/9
 */
public class DnsServiceResponseException extends RuntimeException{

    public DnsServiceResponseException(String message) {
        super(message);
    }

    public DnsServiceResponseException(Throwable cause) {
        super(cause);
    }

    public DnsServiceResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DnsServiceResponseException(String message,
                                       Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
