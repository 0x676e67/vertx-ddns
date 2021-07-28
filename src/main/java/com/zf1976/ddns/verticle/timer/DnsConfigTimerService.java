package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.AbstractDnsAPI;
import com.zf1976.ddns.verticle.DNSServiceType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ant
 * Create by Ant on 2021/7/28 10:01 下午
 */
public class DnsConfigTimerService {

    private final Map<DNSServiceType, AbstractDnsAPI<Object>> dnsAPIMap = new HashMap<>(4);
}
