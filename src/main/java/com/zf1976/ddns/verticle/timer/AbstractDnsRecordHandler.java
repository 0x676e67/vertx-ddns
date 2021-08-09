package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.provider.AliyunDnsRecordProvider;
import com.zf1976.ddns.api.provider.CloudflareDnsRecordProvider;
import com.zf1976.ddns.api.provider.DnspodDnsRecordProvider;
import com.zf1976.ddns.api.provider.HuaweiDnsProvider;
import com.zf1976.ddns.api.provider.DnsRecordProvider;
import com.zf1976.ddns.pojo.DDNSConfig;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ant
 * Create by Ant on 2021/8/7 2:19 PM
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractDnsRecordHandler implements PeriodicDnsRecordHandler{

    protected final Vertx vertx;
    protected final Map<DNSServiceType, DnsRecordProvider> dnsApiMap;


    protected AbstractDnsRecordHandler(List<DDNSConfig> ddnsConfigList, Vertx vertx) {
        this(new ConcurrentHashMap<>(4), vertx);
        for (DDNSConfig config : ddnsConfigList) {
            if (config.getId() != null && config.getSecret() != null) {
                switch (config.getDnsServiceType()) {
                    case ALIYUN -> dnsApiMap.put(DNSServiceType.ALIYUN, new AliyunDnsRecordProvider(config.getId(), config.getSecret(), vertx));
                    case DNSPOD -> dnsApiMap.put(DNSServiceType.DNSPOD, new DnspodDnsRecordProvider(config.getId(), config.getSecret(), vertx));
                    case HUAWEI -> dnsApiMap.put(DNSServiceType.HUAWEI, new HuaweiDnsProvider(config.getId(), config.getSecret(), vertx));
                    case CLOUDFLARE -> dnsApiMap.put(DNSServiceType.CLOUDFLARE, new CloudflareDnsRecordProvider(config.getSecret(), vertx));
                }
            }
        }

    }

    protected AbstractDnsRecordHandler(Map<DNSServiceType, DnsRecordProvider> recordApiMap, Vertx vertx) {
        this.dnsApiMap = recordApiMap;
        if (vertx == null) {
            throw new RuntimeException("Vert.x instance cannot be null");
        }
        this.vertx = vertx;
    }
}
