package com.zf1976.ddns.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author ant
 * Create by Ant on 2021/8/6 12:21 PM
 */
public class LogUtil {

    private static final Logger LOG = LogManager.getLogger("[LogUtil]");

    public static void printInfo(Logger log, String message, Throwable throwable) {
        if (log.isInfoEnabled()) {
            log.info(message, throwable);
        }
    }

    public static void printDebug(Logger log, String message, Throwable throwable) {
        if (log.isDebugEnabled()) {
            log.debug(message, throwable);
        }
    }

    public static void printWarn(Logger log, String message, Throwable throwable) {
        if (log.isWarnEnabled()) {
            log.warn(message, throwable);
        }
    }

    public static void printFatal(Logger log, String message, Throwable throwable) {
        if (log.isFatalEnabled()) {
            log.fatal(message, throwable);
        }
    }

    public static void printError(Logger log, String message, Throwable throwable) {
        if (log.isErrorEnabled()) {
            log.error(message, throwable);
        }
    }

    public static void printTrace(Logger log, String message, Throwable throwable) {
        if (log.isTraceEnabled()) {
            log.trace(message, throwable);
        }
    }

    public static void printInfo(String message, Throwable throwable) {
        printInfo(LOG, message, throwable);
    }

    public static void printDebug(String message, Throwable throwable) {
        printDebug(LOG, message, throwable);
    }

    public static void printWarn(String message, Throwable throwable) {
        printWarn(LOG, message, throwable);
    }

    public static void printFatal(String message, Throwable throwable) {
        printFatal(LOG, message, throwable);
    }

    public static void printError(String message, Throwable throwable) {
        printError(LOG, message, throwable);
    }

    public static void printTrace(String message, Throwable throwable) {
        printTrace(LOG, message, throwable);
    }

}
