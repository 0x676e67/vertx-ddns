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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mac
 * @date 2021/7/6
 */
@SuppressWarnings({"UnusedReturnValue", "RegExpRedundantEscape"})
public final class HttpUtil {

    private static final Logger LOG = LogManager.getLogger("[HttpUtil]");
    public static final Pattern IP_CHECK_PATTERN = Pattern.compile("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}");
    public static final Pattern DOMAIN_PATTERN = Pattern.compile("^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$");
    public static final Pattern IPV4_EXTRACT_PATTERN = Pattern.compile("((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])");
    public static final Pattern IP6_EXTRACT_PATTERN = Pattern.compile("((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))");
    private static final Pattern INNER_IP_PATTERN = Pattern.compile("^(127\\.0\\.0\\.1)|(0\\:0\\:0\\:0\\:0\\:0\\:0\\:1)|(localhost)|(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|(192\\.168\\.\\d{1,3}\\.\\d{1,3})$");
    private static WebClient webClient;
    private static final String DEFAULT_IPV4_API = "https://api-ipv4.ip.sb/ip";
    private static final String DEFAULT_IPV6_API = "https://api-ipv6.ip.sb/ip";
    private static final String[] staticDomain = {"com.cn", "org.cn", "net.cn", "ac.cn", "eu.org"};

    public static void initCustomWebClient(Vertx vertx) {
        HttpUtil.webClient = WebClient.create(vertx);
    }

    /**
     * 获取当前主机公网IP
     *
     * @return {@link String}
     */
    public static Future<String> getCurrentHostIpv4() {
        return getCurrentHostIpv4(DEFAULT_IPV4_API);
    }

    public static Future<String> getCurrentHostIpv6() {
        return getCurrentHostIpv6(DEFAULT_IPV6_API);
    }

    /**
     * 获取当前主机公网IP
     *
     * @return {@link String}
     */
    public static Future<String> getCurrentHostIpv4(String api) {
        return getCurrentHostIp(api, IPV4_EXTRACT_PATTERN);
    }

    public static Future<String> getCurrentHostIpv6(String api) {
        return getCurrentHostIp(api, IP6_EXTRACT_PATTERN);
    }

    /**
     * 自行提供IP查询API获取公网ip
     *
     * @param ipApi api
     * @return ip
     */
    @SuppressWarnings({"LoopStatementThatDoesntLoop", "UnusedAssignment"})
    public static Future<String> getCurrentHostIp(String ipApi, Pattern IP_EXTRACT_PATTERN_PATTERN) {
        if (StringUtil.isEmpty(ipApi)) {
            return Future.succeededFuture();
        }
        return webClient.getAbs(ipApi)
                        .send()
                        .compose(b -> {
                            var body = b.bodyAsString();
                            Matcher mat = IP_EXTRACT_PATTERN_PATTERN.matcher(body);
                            for (int i = 0; (i < body.length()) && mat.find(); i++) {
                                body = mat.group()
                                          .trim();
                                break;
                            }
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Host IP: {}", body);
                            }
                            return Future.succeededFuture(body);
                        });
    }

    /**
     * 判断是否为域名
     *
     * @param domain 域名
     * @return {@link boolean}
     */
    public static boolean isDomain(String domain) {
        return !DOMAIN_PATTERN.matcher(domain).matches();
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
        try {
            final var uri = URI.create(url);
            return uri.getHost() != null && uri.getAuthority() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 提取顶级主域名跟域名记录
     * 比如：www.baidu.com -> 顶级域名：baidu.com  记录：www 、   a.b.baidu.com -> 顶级域名：baidu.com   记录：a.b
     *
     * @param domain 域名
     * @return {@link String[]}
     */
    public static String[] extractDomain(String domain) {
        final var split = domain.split("\\.");
        final var length = split.length;
        if (StringUtil.isEmpty(domain) || isDomain(domain) || length <= 1 ){
            if (split[0] != "*") {
                throw new RuntimeException("The domain name does not meet the specification");
            }
        }

        String mainDomain = split[length-2] + "." + split[length-1];
        String record = "";
        for (String staticDomain : staticDomain) {
            if (ObjectUtil.nullSafeEquals(staticDomain, mainDomain)) {
                mainDomain = split[length - 3] + "." + mainDomain;
                break;
            }
        }
        final var mainDomainIndex = domain.lastIndexOf(mainDomain);
        if (mainDomainIndex > 0 ) {
            record = domain.substring(0, mainDomainIndex - 1);
        }

        return new String[] {mainDomain, record};
    }

    /**
     * 获取真实ip地址
     *
     * @return ip
     */
    public static String getIpAddress(final HttpServerRequest request) {
        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址
        String ip = request.getHeader("x-forwarded-for");
        if (LOG.isDebugEnabled()) {
            LOG.info("x-forwarded-for ip: " + ip);
        }
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if(ip.contains(",")){
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            if (LOG.isDebugEnabled()) {
                LOG.info("Proxy-Client-IP ip: " + ip);
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            if (LOG.isDebugEnabled()) {
                LOG.info("WL-Proxy-Client-IP ip: " + ip);
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            if (LOG.isDebugEnabled()) {
                LOG.info("HTTP_CLIENT_IP ip: " + ip);
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (LOG.isDebugEnabled()) {
                LOG.info("HTTP_X_FORWARDED_FOR ip: " + ip);
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
            if (LOG.isDebugEnabled()) {
                LOG.info("X-Real-IP ip: " + ip);
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.remoteAddress().hostAddress();
            if (LOG.isDebugEnabled()) {
                LOG.info("getRemoteAddress ip: " + ip);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.info("Get client ip: " + ip);
        }
        return ip;
    }


    /**
     * Determine whether it is an intranet ip
     *
     * @param ip IP
     * @return {@link boolean}
     */
    public static boolean isInnerIp(String ip) {
        return INNER_IP_PATTERN.matcher(ip).find();
    }

    public static Future<String> getNetworkCardIpv4Ip(String card) {
        for (String cardAndIp : getNetworkIpv4List()) {
            if (cardAndIp.substring(0, cardAndIp.indexOf('(')).equals(card)) {
                return Future.succeededFuture(cardAndIp.substring(cardAndIp.indexOf('(') + 1, cardAndIp.lastIndexOf(')')));
            }
        }
        return Future.failedFuture("Not assigned to IPv4");
    }

    public static Future<String> getNetworkCardIpv6Ip(String card) {
        for (String cardAndIp : getNetworkIpv6List()) {
            if (cardAndIp.substring(0, cardAndIp.indexOf('(')).equals(card)) {
                return Future.succeededFuture(cardAndIp.substring(cardAndIp.indexOf('(') + 1, cardAndIp.lastIndexOf(')')));
            }
        }
        return Future.failedFuture("Not assigned to IPv6");
    }

    /**
     * get ipv4 list
     *
     * @return {@link List <String>}
     */
    public static List<String> getNetworkIpv4List() {
        Enumeration<NetworkInterface> netInterfaces;
        List<String> networkIpList = new ArrayList<>();
        try {
            // Get all network cards
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            // Traverse each network card and get the ip
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (!ip.isLoopbackAddress() && ip instanceof Inet4Address && ip.getHostAddress().indexOf(':') == -1) {
                        networkIpList.add(ni.getName() + "(" + ip.getHostAddress() + ")");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return networkIpList;
    }

    /**
     * get ipv6 list
     *
     * @return {@link List<String>}
     */
    public static List<String> getNetworkIpv6List() {
        Enumeration<NetworkInterface> netInterfaces;
        List<String> networkIpList = new ArrayList<>();
        try {
            // Get all network cards
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            // Traverse each network card and get the ip
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (!ip.isLoopbackAddress() && ip instanceof Inet6Address && ip.getHostAddress().indexOf(':') != -1) {
                        String hostAddress = ip.getHostAddress();
                        networkIpList.add(ni.getName() + "(" + hostAddress.substring(0, hostAddress.indexOf('%')) + ")");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return networkIpList;
    }

    @Deprecated
    public static String getLocalIPv6Address() throws SocketException {
        InetAddress inetAddress = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        outer:
        while (networkInterfaces.hasMoreElements()) {
            final var inetAddresses = networkInterfaces.nextElement().getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                inetAddress = inetAddresses.nextElement();
                // Check whether this address is an IPV 6 address and whether it is a reserved address
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address && !isReservedAddress(inetAddress)) {
                    break outer;
                }
            }
        }
        assert inetAddress != null;
        String hostAddress = inetAddress.getHostAddress();
        // Filter card
        int index = hostAddress.indexOf('%');
        if (index > 0) {
            hostAddress = hostAddress.substring(0, index);
        }
        return hostAddress;
    }

    private static boolean isReservedAddress(InetAddress inetAddress) {
        return inetAddress.isAnyLocalAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress();
    }
}
