package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.impl.AliyunDnsApi;
import com.zf1976.ddns.api.impl.CloudflareDnsApi;
import com.zf1976.ddns.api.impl.DnspodDnsApi;
import com.zf1976.ddns.api.impl.HuaweiDnsApi;
import com.zf1976.ddns.api.signer.algorithm.DnsRecordApi;
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
    protected final Map<DNSServiceType, DnsRecordApi> dnsApiMap;


    protected AbstractDnsRecordHandler(List<DDNSConfig> ddnsConfigList, Vertx vertx) {
        this(new ConcurrentHashMap<>(4), vertx);
        for (DDNSConfig config : ddnsConfigList) {
            if (config.getId() != null && config.getSecret() != null) {
                switch (config.getDnsServiceType()) {
                    case ALIYUN -> dnsApiMap.put(DNSServiceType.ALIYUN, new AliyunDnsApi(config.getId(), config.getSecret(), vertx));
                    case DNSPOD -> dnsApiMap.put(DNSServiceType.DNSPOD, new DnspodDnsApi(config.getId(), config.getSecret(), vertx));
                    case HUAWEI -> dnsApiMap.put(DNSServiceType.HUAWEI, new HuaweiDnsApi(config.getId(), config.getSecret(), vertx));
                    case CLOUDFLARE -> dnsApiMap.put(DNSServiceType.CLOUDFLARE, new CloudflareDnsApi(config.getSecret(), vertx));
                }
            }
        }

    }

    protected AbstractDnsRecordHandler(Map<DNSServiceType, DnsRecordApi> recordApiMap, Vertx vertx) {
        this.dnsApiMap = recordApiMap;
        if (vertx == null) {
            throw new RuntimeException("Vert.x instance cannot be null");
        }
        this.vertx = vertx;
    }
}
