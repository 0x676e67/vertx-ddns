package com.zf1976.ddns.verticle.handler;

import com.zf1976.ddns.config.DnsConfig;
import com.zf1976.ddns.enums.DnsProviderType;

/**
 * @author ant
 * Create by Ant on 2021/8/7 2:17 PM
 */
public interface ResolveDnsRecordHandler {

    void parserDnsRecordForIpv4(DnsProviderType dnsProviderType, DnsConfig.Ipv4Config ipv4Config);

    void resolveDnsRecordForIpv6(DnsProviderType dnsProviderType, DnsConfig.Ipv6Config ipv6Config);

}
