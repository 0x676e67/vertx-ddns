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

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mac
 * 2021/7/14
 */
public class ApiURLEncoderUtil {
    public final static Charset URL_ENCODING = StandardCharsets.UTF_8;
    private static final Pattern ENCODED_CHARACTERS_PATTERN;

    static {
        ENCODED_CHARACTERS_PATTERN = Pattern.compile(Pattern.quote("+") + "|" + Pattern.quote("*") + "|" + Pattern.quote("%7E") + "|" + Pattern.quote("%2F"));
    }

    public static String huaweiPercentEncode(String value, boolean path) {
        if (value == null) {
            return "";
        } else {
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
        }
    }

    public static String encode(String value) {
        return URLEncoder.encode(value, URL_ENCODING);
    }

    public static String aliyunPercentEncode(String value) {
        // 这里官方文档签名%20会请求间歇性失败，需改成%2B
        return value != null ? encode(value).replace("+", "%2B")
                                            .replace("*", "%2A")
                                            .replace("%7E", "~") : null;
    }

}
