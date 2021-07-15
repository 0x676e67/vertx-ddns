package com.zf1976.ddns.util;

/**
 * @author mac
 * @date 2021/7/14
 */
public class DataTypeConverterUtil {

    private static final char[] encodeMap = initEncodeMap();

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
                case 1:
                    buf[ptr++] = encode(input[i] >> 2);
                    buf[ptr++] = encode(((input[i]) & 0x3) << 4);
                    buf[ptr++] = '=';
                    buf[ptr++] = '=';
                    break;
                case 2:
                    buf[ptr++] = encode(input[i] >> 2);
                    buf[ptr++] = encode(
                            ((input[i] & 0x3) << 4) |
                                    ((input[i + 1] >> 4) & 0xF));
                    buf[ptr++] = encode((input[i + 1] & 0xF) << 2);
                    buf[ptr++] = '=';
                    break;
                default:
                    buf[ptr++] = encode(input[i] >> 2);
                    buf[ptr++] = encode(
                            ((input[i] & 0x3) << 4) |
                                    ((input[i + 1] >> 4) & 0xF));
                    buf[ptr++] = encode(
                            ((input[i + 1] & 0xF) << 2) |
                                    ((input[i + 2] >> 6) & 0x3));
                    buf[ptr++] = encode(input[i + 2] & 0x3F);
                    break;
            }
        }
        return ptr;
    }

    public static char encode(int i) {
        return encodeMap[i & 0x3F];
    }
}
