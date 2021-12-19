package com.zf1976.ddns.api.provider.exception;

/**
 * @author mac
 * 2021/8/11 星期三 5:07 下午
 */
@SuppressWarnings("unused")
public class ResolvedDomainException extends RuntimeException{

    public ResolvedDomainException(Throwable cause) {
        super(cause);
    }

    public ResolvedDomainException(String message) {
        super(message);
    }

    public ResolvedDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResolvedDomainException(String message,
                                   Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
