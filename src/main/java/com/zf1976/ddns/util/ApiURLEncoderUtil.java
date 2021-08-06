package com.zf1976.ddns.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mac
 * @date 2021/7/14
 */
public class ApiURLEncoderUtil {
    public final static String URL_ENCODING = "UTF-8";
    private static final Pattern ENCODED_CHARACTERS_PATTERN;

    static {
        ENCODED_CHARACTERS_PATTERN = Pattern.compile(Pattern.quote("+") + "|" + Pattern.quote("*") + "|" + Pattern.quote("%7E") + "|" + Pattern.quote("%2F"));
    }

    public static String huaweiPercentEncode(String value, boolean path) {
        if (value == null) {
            return "";
        } else {
            try {
                String encoded = URLEncoder.encode(value, URL_ENCODING);
                Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded);

                StringBuilder buffer;
                String replacement;
                for (buffer = new StringBuilder(encoded.length()); matcher.find(); matcher.appendReplacement(buffer, replacement)) {
                    replacement = matcher.group(0);
                    if ("+".equals(replacement)) {
                        replacement = "%20";
                    } else if ("*".equals(replacement)) {
                        replacement = "%2A";
                    } else if ("%7E".equals(replacement)) {
                        replacement = "~";
                    } else if (path && "%2F".equals(replacement)) {
                        replacement = "/";
                    }
                }

                matcher.appendTail(buffer);
                return buffer.toString();
            } catch (UnsupportedEncodingException var6) {
                throw new RuntimeException(var6);
            }
        }
    }

    public static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, URL_ENCODING);
    }

    public static String aliyunPercentEncode(String value) throws UnsupportedEncodingException {
        // 这里官方文档签名%20会请求间歇性失败，需改成%2B
        return value != null ? encode(value).replace("+", "%2B")
                                            .replace("*", "%2A")
                                            .replace("%7E", "~") : null;
    }

}
