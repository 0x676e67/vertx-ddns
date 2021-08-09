package com.zf1976.ddns.api.provider.exception;

/**
 * @author mac
 * 2021/8/9 星期一 10:36 下午
 */
public class InvalidDnsCredentialException extends RuntimeException{

    public InvalidDnsCredentialException(Throwable cause) {
        super(cause);
    }

    public InvalidDnsCredentialException(String message) {
        super(message);
    }

    public InvalidDnsCredentialException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDnsCredentialException(String message,
                                         Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
