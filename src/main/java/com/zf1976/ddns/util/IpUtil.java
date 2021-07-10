package com.zf1976.ddns.util;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/10
 */
public final class IpUtil {

    public static List<String> getNetworkIpv4List() {
        Enumeration<NetworkInterface> netInterfaces;
        List<String> networkIpList = new ArrayList<>();
        try {
            // 拿到所有网卡
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            // 遍历每个网卡，拿到ip
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

    public static List<String> getNetworkIpv6List() {
        Enumeration<NetworkInterface> netInterfaces;
        List<String> networkIpList = new ArrayList<>();
        try {
            // 拿到所有网卡
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            // 遍历每个网卡，拿到ip
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (!ip.isLoopbackAddress() && ip instanceof Inet6Address && ip.getHostAddress().indexOf(':') == -1) {
                        networkIpList.add(ni.getName() + "(" + ip.getHostAddress() + ")");
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
                //检查此地址是否是IPv6地址以及是否是保留地址
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address && !isReservedAddress(inetAddress)) {
                    break outer;
                }
            }
        }
        assert inetAddress != null;
        String hostAddress = inetAddress.getHostAddress();
        //过滤网卡
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
