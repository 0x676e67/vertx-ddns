package com.zf1976.ddns.verticle.handler;

import com.zf1976.ddns.api.enums.DnsProviderType;
import com.zf1976.ddns.pojo.DnsConfig;

/**
 * @author ant
 * Create by Ant on 2021/8/7 2:17 PM
 */
public interface ResolveDnsRecordHandler {

    void parserDnsRecordForIpv4(DnsProviderType dnsProviderType, DnsConfig.Ipv4Config ipv4Config);

    void parserDnsRecordForIpv6(DnsProviderType dnsProviderType, DnsConfig.Ipv6Config ipv6Config);

}
