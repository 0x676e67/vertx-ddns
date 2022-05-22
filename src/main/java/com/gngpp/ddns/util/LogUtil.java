/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author ant
 * Create by Ant on 2021/8/6 12:21 PM
 */
@SuppressWarnings("unused")
public class LogUtil {

    private static final Logger LOG = LogManager.getLogger("[LogUtil]");

    public static void printInfo(Logger log, Object message, Throwable throwable) {
        if (log.isInfoEnabled()) {
            if (throwable != null) {
                log.info(message, throwable);
            } else {
                log.info(message);
            }
        }
    }

    public static void printDebug(Logger log, Object message, Throwable throwable) {
        if (log.isDebugEnabled()) {
            if (throwable != null) {
                log.debug(message, throwable);
            } else {
                log.debug(message);
            }
        }
    }

    public static void printWarn(Logger log, Object message, Throwable throwable) {
        if (log.isWarnEnabled()) {
            if (throwable != null) {
                log.warn(message, throwable);
            } else {
                log.warn(message);
            }
        }
    }

    public static void printFatal(Logger log, Object message, Throwable throwable) {
        if (log.isFatalEnabled()) {
            if (throwable != null) {
                log.fatal(message, throwable);
            } else {
                log.fatal(message);
            }
        }
    }

    public static void printError(Logger log, Object message, Throwable throwable) {
        if (log.isErrorEnabled()) {
            if (throwable != null) {
                log.error(message, throwable);
            } else {
                log.error(message);
            }
        }
    }

    public static void printTrace(Logger log, Object message, Throwable throwable) {
        if (log.isTraceEnabled()) {
            if (throwable != null) {
                log.trace(message, throwable);
            } else {
                log.trace(message);
            }
        }
    }

    public static void printInfo(Object message, Throwable throwable) {
        printInfo(LOG, message, throwable);
    }

    public static void printDebug(Object message, Throwable throwable) {
        printDebug(LOG, message, throwable);
    }

    public static void printWarn(Object message, Throwable throwable) {
        printWarn(LOG, message, throwable);
    }

    public static void printFatal(Object message, Throwable throwable) {
        printFatal(LOG, message, throwable);
    }

    public static void printError(Object message, Throwable throwable) {
        printError(LOG, message, throwable);
    }

    public static void printTrace(Object message, Throwable throwable) {
        printTrace(LOG, message, throwable);
    }

    public static void printInfo(Logger log, Object message) {
        printInfo(log, message, (Throwable) null);
    }

    public static void printDebug(Logger log, Object message) {
        printDebug(log, message, (Throwable) null);
    }

    public static void printWarn(Logger log, Object message) {
        printWarn(log, message, (Throwable) null);
    }

    public static void printFatal(Logger log, Object message) {
        printFatal(log, message, (Throwable) null);
    }

    public static void printError(Logger log, Object message) {
        printError(log, message, (Throwable) null);
    }

    public static void printTrace(Logger log, Object message) {
        printTrace(log, message, (Throwable) null);
    }

    public static void printInfo(Object message) {
        printInfo(LOG, message, (Throwable) null);
    }


    public static void printDebug(Object message) {
        printDebug(LOG, message, (Throwable) null);
    }

    public static void printWarn(Object message) {
        printWarn(LOG, message, (Throwable) null);
    }

    public static void printFatal(Object message) {
        printFatal(LOG, message, (Throwable) null);
    }

    public static void printError(Object message) {
        printError(LOG, message, (Throwable) null);
    }

    public static void printTrace(Object message) {
        printTrace(LOG, message, (Throwable) null);
    }

}
