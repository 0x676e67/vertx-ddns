package com.zf1976.ddns.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

/**
 * @author mac
 * @date 2021/7/6
 */
public final class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger("[HttpUtil]");
    public static final String IP_REGEXP = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
    public static final Pattern IP_PATTERN = Pattern.compile(IP_REGEXP);
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final HttpRequest IP_REQUEST_1 = HttpRequest.newBuilder()
                                                               .GET()
                                                               .uri(URI.create("https://api-ipv4.ip.sb/ip"))
                                                               .build();
    /**
     * 获取当前主机公网IP
     *
     * @return {@link String}
     */
    public static String getCurrentHostIp() {
        String ip = "";
        try {
            final var body = HTTP_CLIENT.send(IP_REQUEST_1, HttpResponse.BodyHandlers.ofString()).body();
            ip = body.trim();
            if (!IP_PATTERN.matcher(ip).matches()) {
                ip = "";
            }
            log.info("Host IP: {}", ip);
            return ip;
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return "";
    }

}
