package com.zf1976.ddns.util;

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
