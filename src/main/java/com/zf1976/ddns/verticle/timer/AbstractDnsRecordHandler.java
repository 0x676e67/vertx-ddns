package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.provider.AliyunDnsProvider;
import com.zf1976.ddns.api.provider.CloudflareDnsProvider;
import com.zf1976.ddns.api.provider.DnspodDnsProvider;
import com.zf1976.ddns.api.provider.HuaweiDnsProvider;
import com.zf1976.ddns.api.provider.DnsRecordProvider;
import com.zf1976.ddns.pojo.*;
import com.zf1976.ddns.api.enums.DnsProviderType;
import com.zf1976.ddns.pojo.vo.DnsRecordVo;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
import io.vertx.core.Vertx;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    protected void checkIp(String ip) {
        if (!HttpUtil.isIp(ip)) {
            throw new RuntimeException("ip：" + ip + " unqualified");
        }
    }

    protected void checkDomain(String domain) {
        if (HttpUtil.isDomain(domain)) {
            throw new RuntimeException("domain：" + domain + " unqualified");
        }
    }

    protected Boolean deleteResultGenericsResultHandler(Object obj) {
        if (obj instanceof DnspodDataResult dnspodDataResult) {
            return dnspodDataResult.getResponse().getError() == null;
        } else if (obj instanceof CloudflareDataResult cloudflareDataResult) {
            return cloudflareDataResult.getSuccess();
        } else return obj instanceof AliyunDataResult || obj instanceof HuaweiDataResult;
    }

    protected List<DnsRecordVo> findGenericsResultHandler(Object obj, String domain) {
        List<DnsRecordVo> recordVoList = new LinkedList<>();
        if (obj instanceof AliyunDataResult result && result.getDomainRecords() != null) {
            final var domainRecords = result.getDomainRecords()
                                            .getRecordList();
            for (AliyunDataResult.Record record : domainRecords) {
                final var recordVo = DnsRecordVo.newBuilder()
                                                .withId(record.getRecordId())
                                                .withDomain(record.getDomainName())
                                                .withRr(record.getRr())
                                                .withValue(record.getValue())
                                                .build();
                recordVoList.add(recordVo);
            }
        }

        if (obj instanceof DnspodDataResult result && result.getResponse() != null) {
            final var recordList = result.getResponse()
                                         .getRecordList();
            if (!CollectionUtil.isEmpty(recordList)) {
                for (DnspodDataResult.RecordList record : recordList) {
                    final var extractDomain = HttpUtil.extractDomain(domain);
                    final var recordVo = DnsRecordVo.newBuilder()
                                                    .withId(String.valueOf(record.getRecordId()))
                                                    .withDomain(extractDomain[0])
                                                    .withRr(record.getName())
                                                    .withValue(record.getValue())
                                                    .build();
                    recordVoList.add(recordVo);
                }
            }
        }

        if (obj instanceof HuaweiDataResult result) {
            final var recordList = result.getRecordsets();
            if (!CollectionUtil.isEmpty(recordList)) {
                for (HuaweiDataResult.Recordsets record : recordList) {
                    final var huaweiDomain = record.getName()
                                                   .substring(0, record.getName()
                                                                       .length() - 1);
                    final var extractDomain = HttpUtil.extractDomain(huaweiDomain);
                    final var recordVo = DnsRecordVo.newBuilder()
                                                    .withId(record.getId())
                                                    .withDomain(extractDomain[0])
                                                    .withRr(Objects.equals(extractDomain[1], "") ? "@" : extractDomain[1])
                                                    .withValue(CollectionUtil.isEmpty(record.getRecords()) ? null : record.getRecords()
                                                                                                                          .get(0))
                                                    .build();
                    recordVoList.add(recordVo);
                }
            }
        }

        if (obj instanceof CloudflareDataResult result && result.getSuccess()) {
            final var resultList = result.getResult();
            if (!CollectionUtil.isEmpty(resultList)) {
                for (CloudflareDataResult.Result record : resultList) {
                    final var extractDomain = HttpUtil.extractDomain(record.getName());
                    final var recordVo = DnsRecordVo.newBuilder()
                                                    .withId(record.getId())
                                                    .withDomain(record.getZoneName())
                                                    .withRr(Objects.equals(extractDomain[1], "") ? "@" : extractDomain[1])
                                                    .withValue(record.getContent())
                                                    .build();
                    recordVoList.add(recordVo);
                }
            }
        }
        return recordVoList;
    }

}
