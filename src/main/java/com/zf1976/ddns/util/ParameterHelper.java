package com.zf1976.ddns.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParameterHelper {

    private final static String TIME_ZONE = "GMT+8";
    private final static String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final static String FORMAT_RFC2616 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public ParameterHelper() {
    }


    public static void main(String[] args) {
        System.out.println(getISO8601Time(new Date()));
    }

    public static String getUniqueNonce() {
        StringBuilder uniqueNonce = new StringBuilder();
        UUID uuid = UUID.randomUUID();
        uniqueNonce.append(uuid);
        uniqueNonce.append(System.currentTimeMillis());
        uniqueNonce.append(Thread.currentThread()
                                 .getId());
        uniqueNonce.append(new Random().nextDouble());
        return uniqueNonce.toString();
    }

    public static String getISO8601Time(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.format(date);
    }

    public static String getRFC2616Date(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_RFC2616, Locale.ENGLISH);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.format(date);
    }

    public static Date parse(String strDate) throws ParseException {
        if (null == strDate || "".equals(strDate)) {
            return null;
        }
        // The format contains 4 '
        if (strDate.length() == FORMAT_ISO8601.length() - 4) {
            return parseISO8601(strDate);
        } else if (strDate.length() == FORMAT_RFC2616.length()) {
            return parseRFC2616(strDate);
        }
        return null;
    }

    public static Date parseISO8601(String strDate) throws ParseException {
        if (null == strDate || "".equals(strDate)) {
            return null;
        }

        // The format contains 4 ' symbol
        if (strDate.length() != (FORMAT_ISO8601.length() - 4)) {
            return null;
        }

        SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.parse(strDate);
    }

    public static Date parseRFC2616(String strDate) throws ParseException {
        if (null == strDate || "".equals(strDate) || strDate.length() != FORMAT_RFC2616.length()) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_RFC2616, Locale.ENGLISH);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.parse(strDate);
    }

}
