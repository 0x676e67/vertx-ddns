package com.zf1976.ddns.api.huawei;

import java.util.Locale;

/**
 * @author mac
 * @date 2021/7/24
 */
public class BinaryUtils {
    public BinaryUtils() {
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        byte[] var2 = data;
        int var3 = data.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                sb.append("0");
            } else if (hex.length() == 8) {
                hex = hex.substring(6);
            }

            sb.append(hex);
        }

        return sb.toString()
                 .toLowerCase(Locale.getDefault());
    }
}
