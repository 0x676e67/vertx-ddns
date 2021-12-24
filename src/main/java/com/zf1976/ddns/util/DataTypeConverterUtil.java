/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
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

package com.zf1976.ddns.util;

/**
 * @author mac
 * @date 2021/7/14
 */
public class DataTypeConverterUtil {

    private static final char[] encodeMap = initEncodeMap();
    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private static char[] initEncodeMap() {
        char[] map = new char[64];
        int i;
        for (i = 0; i < 26; i++) map[i] = (char) ('A' + i);
        for (i = 26; i < 52; i++) map[i] = (char) ('a' + (i - 26));
        for (i = 52; i < 62; i++) map[i] = (char) ('0' + (i - 52));
        map[62] = '+';
        map[63] = '/';

        return map;
    }

    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[b >> 4 & 15]);
            r.append(hexCode[b & 15]);
        }
        return r.toString();
    }


    public static String _printBase64Binary(byte[] input) {
        return _printBase64Binary(input, 0, input.length);
    }

    public static String _printBase64Binary(byte[] input, int offset, int len) {
        char[] buf = new char[((len + 2) / 3) * 4];
        int ptr = _printBase64Binary(input, offset, len, buf, 0);
        assert ptr == buf.length;
        return new String(buf);
    }

    public static int _printBase64Binary(byte[] input, int offset, int len, char[] buf, int ptr) {
        for (int i = offset; i < len; i += 3) {
            switch (len - i) {
                case 1 -> {
                    buf[ptr++] = encode(input[i] >> 2);
                    buf[ptr++] = encode(((input[i]) & 0x3) << 4);
                    buf[ptr++] = '=';
                    buf[ptr++] = '=';
                }
                case 2 -> {
                    buf[ptr++] = encode(input[i] >> 2);
                    buf[ptr++] = encode(
                            ((input[i] & 0x3) << 4) |
                                    ((input[i + 1] >> 4) & 0xF));
                    buf[ptr++] = encode((input[i + 1] & 0xF) << 2);
                    buf[ptr++] = '=';
                }
                default -> {
                    buf[ptr++] = encode(input[i] >> 2);
                    buf[ptr++] = encode(
                            ((input[i] & 0x3) << 4) |
                                    ((input[i + 1] >> 4) & 0xF));
                    buf[ptr++] = encode(
                            ((input[i + 1] & 0xF) << 2) |
                                    ((input[i + 2] >> 6) & 0x3));
                    buf[ptr++] = encode(input[i + 2] & 0x3F);
                }
            }
        }
        return ptr;
    }

    public static char encode(int i) {
        return encodeMap[i & 0x3F];
    }
}
