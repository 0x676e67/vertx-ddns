package com.zf1976.ddns.api.singer.aliyun;

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
        return value != null ? URLEncoder.encode(value, URL_ENCODING).replace("+", "%20")
                                         .replace("*", "%2A").replace("%7E", "~") : null;
    }
}
