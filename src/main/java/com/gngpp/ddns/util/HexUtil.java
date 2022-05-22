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

/**
 * @author ant
 * Create by Ant on 2021/7/31 3:18 PM
 */

public class HexUtil {
    public HexUtil() {
    }

    public static String byteArr2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();

        for (byte b : buf) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            sb.append(hex);
        }

        return sb.toString();
    }

    public static byte[] hexStr2ByteArr(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() >> 1];

            for (int i = 0; i < hexStr.length() >> 1; ++i) {
                int high = Integer.parseInt(hexStr.substring(i << 1, (i << 1) + 1), 16);
                int low = Integer.parseInt(hexStr.substring((i << 1) + 1, i + 1 << 1), 16);
                result[i] = (byte) ((high << 4) + low);
            }

            return result;
        }
    }
}

