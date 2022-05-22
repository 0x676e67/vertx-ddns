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

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author ant
 * Create by Ant on 2021/7/31 2:59 PM
 */
@SuppressWarnings("all")
public class Base64Util {
    public Base64Util() {
    }

    public static byte[] decryptBASE64(byte[] encryptedData) {
        return Base64.getDecoder()
                     .decode(encryptedData);
    }

    public static byte[] decryptBASE64(String encryptedData) {
        try {
            return decryptBASE64(encryptedData.getBytes("utf-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("不支持的编码方式：" + var2);
        }
    }

    public static String decryptToString(String encryptedData) {
        try {
            return decryptToString(encryptedData.getBytes("utf-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("不支持的编码方式：" + var2);
        }
    }

    public static String decryptToString(byte[] encryptedData) {
        return new String(decryptBASE64(encryptedData));
    }

    public static byte[] encryptBASE64(byte[] decryptedData) {
        return Base64.getEncoder()
                     .encode(decryptedData);
    }

    public static byte[] encryptBASE64(String plaintext) {
        try {
            return encryptBASE64(plaintext.getBytes("utf-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("不支持的编码方式：" + var2);
        }
    }

    public static String encryptToString(String plaintext) {
        try {
            return encryptToString(plaintext.getBytes("utf-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("不支持的编码方式：" + var2);
        }
    }

    public static String encryptToString(byte[] plainByteArr) {
        return new String(encryptBASE64(plainByteArr));
    }
}
