package com.zf1976.ddns.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mac
 * @date 2021/7/6
 */
@SuppressWarnings({"UnusedReturnValue", "RegExpRedundantEscape"})
public final class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger("[HttpUtil]");
    public static final String IP_CHECK_REGEXP = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
    public static final String IP_EXTRACT_REGEXP = "(\\d{1,3}\\.){3}\\d{1,3}";
    public static final String DOMAIN_REGEXP = "^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$";
    public static final String URL_REGEXP = "^(?=^.{3,255}$)(http(s)?:\\/\\/)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(\\/\\w+\\.\\w+)*([\\?&]\\w+=\\w*)*$";
    public static final Pattern IP_CHECK_PATTERN = Pattern.compile(IP_CHECK_REGEXP);
    public static final Pattern IP_EXTRACT_PATTERN = Pattern.compile(IP_EXTRACT_REGEXP);
    public static final Pattern DOMAIN_PATTERN = Pattern.compile(DOMAIN_REGEXP);
    public static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);
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
        String ip;
        try {
            final var body = HTTP_CLIENT.send(IP_REQUEST_1, HttpResponse.BodyHandlers.ofString()).body();
            ip = body.trim();
            if (!IP_CHECK_PATTERN.matcher(ip).matches()) {
                ip = "";
            }
            log.info("Host IP: {}", ip);
            return ip;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "";
    }

    /**
     * 自行提供IP查询API获取公网ip
     * @param ipApi api
     * @return ip
     */
    @SuppressWarnings({"LoopStatementThatDoesntLoop", "UnusedAssignment"})
    public static String getCurrentHostIp(String ipApi) {
        if (StringUtil.isEmpty(ipApi)) {
            return getCurrentHostIp();
        }

        try {
            final var request = HttpRequest.newBuilder()
                                           .GET()
                                           .uri(URI.create(ipApi))
                                           .build();
            var ip = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            Matcher mat = IP_EXTRACT_PATTERN.matcher(ip);
            for (int i = 0; (i < ip.length()) && mat.find(); i++) {
                ip = mat.group().trim();
                break;
            }
            log.info("Host IP: {}", ip);
            return ip;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "";
    }

    /**
     * 判断是否为域名
     *
     * @param domain 域名
     * @return {@link boolean}
     */
    public static boolean isDomain(String domain) {
        return DOMAIN_PATTERN.matcher(domain).matches();
    }

    /**
     * 判断ip格式是否正确
     *
     * @param ip ip
     * @return {@link boolean}
     */
    public static boolean isIp(String ip) {
        return IP_CHECK_PATTERN.matcher(ip).matches();
    }

    /**
     * 判断url格式是否正确
     *
     * @param url url
     * @return {@link boolean}
     */
    public static boolean isURL(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * 提取顶级主域名跟域名记录
     * 比如：www.baidu.com -> 顶级域名：baidu.com  记录：www 、   a.b.baidu.com -> 顶级域名：baidu.com   记录：a.b
     *
     * @param domain 域名
     * @return {@link String[]}
     */
    public static String[] extractDomain(String domain) {
        if (StringUtil.isEmpty(domain) || !isDomain(domain)) {
            throw new RuntimeException("The domain name does not meet the specification");
        }
        final var split = domain.split("\\.");
        final var length = split.length;
        final String mainDomain = split[length-2] + "." + split[length-1];
        String record = "";
        if (length > 2) {
            final var mainDomainIndex = domain.lastIndexOf(mainDomain);
            record = domain.substring(0, mainDomainIndex - 1);
        }
        return new String[] {mainDomain, record};
    }

}
