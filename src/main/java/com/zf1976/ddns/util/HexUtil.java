package com.zf1976.ddns.util;

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

