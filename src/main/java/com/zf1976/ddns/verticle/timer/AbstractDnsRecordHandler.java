package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.provider.AliyunDnsProvider;
import com.zf1976.ddns.api.provider.CloudflareDnsProvider;
import com.zf1976.ddns.api.provider.DnspodDnsProvider;
import com.zf1976.ddns.api.provider.HuaweiDnsProvider;
import com.zf1976.ddns.api.provider.DnsRecordProvider;
import com.zf1976.ddns.pojo.DnsConfig;
import com.zf1976.ddns.api.enums.DnsProviderType;
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
    protected final Map<DnsProviderType, DnsRecordProvider> dnsApiMap;


    protected AbstractDnsRecordHandler(List<DnsConfig> ddnsConfigList, Vertx vertx) {
        this(new ConcurrentHashMap<>(4), vertx);
        for (DnsConfig config : ddnsConfigList) {
            if (config.getId() != null && config.getSecret() != null) {
                switch (config.getDnsProviderType()) {
                    case ALIYUN -> dnsApiMap.put(DnsProviderType.ALIYUN, new AliyunDnsProvider(config.getId(), config.getSecret(), vertx));
                    case DNSPOD -> dnsApiMap.put(DnsProviderType.DNSPOD, new DnspodDnsProvider(config.getId(), config.getSecret(), vertx));
                    case HUAWEI -> dnsApiMap.put(DnsProviderType.HUAWEI, new HuaweiDnsProvider(config.getId(), config.getSecret(), vertx));
                    case CLOUDFLARE -> dnsApiMap.put(DnsProviderType.CLOUDFLARE, new CloudflareDnsProvider(config.getSecret(), vertx));
                }
            }
        }

    }

    protected AbstractDnsRecordHandler(Map<DnsProviderType, DnsRecordProvider> recordApiMap, Vertx vertx) {
        this.dnsApiMap = recordApiMap;
        if (vertx == null) {
            throw new RuntimeException("Vert.x instance cannot be null");
        }
        this.vertx = vertx;
    }
}
