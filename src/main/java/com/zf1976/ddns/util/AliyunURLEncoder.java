package com.zf1976.ddns.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author mac
 * @date 2021/7/14
 */
public class AliyunURLEncoder {
    public final static String URL_ENCODING = "UTF-8";

    public static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, URL_ENCODING);
    }

    public static String percentEncode(String value) throws UnsupportedEncodingException {
        // 这里官方文档签名%20会请求间歇性失败，需改成%2B
        return value != null ? URLEncoder.encode(value, URL_ENCODING)
                                         .replace("+", "%2B")
                                         .replace("*", "%2A")
                                         .replace("%7E", "~") : null;
    }
}
