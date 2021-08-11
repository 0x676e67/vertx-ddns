package com.zf1976.ddns.verticle.service;

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
import com.zf1976.ddns.verticle.timer.PeriodicHandler;
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
public abstract class AbstractDnsRecordService implements PeriodicHandler, DnsRecordService {

    protected final Vertx vertx;
    protected final Map<DnsProviderType, DnsRecordProvider> providerMap;


    protected AbstractDnsRecordService(List<DnsConfig> dnsConfigList, Vertx vertx) {
        this(new ConcurrentHashMap<>(4), vertx);
        for (DnsConfig config : dnsConfigList) {
            if (config.getId() != null && config.getSecret() != null) {
                switch (config.getDnsProviderType()) {
                    case ALIYUN -> providerMap.put(DnsProviderType.ALIYUN, new AliyunDnsProvider(config.getId(), config.getSecret(), vertx));
                    case DNSPOD -> providerMap.put(DnsProviderType.DNSPOD, new DnspodDnsProvider(config.getId(), config.getSecret(), vertx));
                    case HUAWEI -> providerMap.put(DnsProviderType.HUAWEI, new HuaweiDnsProvider(config.getId(), config.getSecret(), vertx));
                    case CLOUDFLARE -> providerMap.put(DnsProviderType.CLOUDFLARE, new CloudflareDnsProvider(config.getSecret(), vertx));
                }
            }
        }

    }

    protected AbstractDnsRecordService(Map<DnsProviderType, DnsRecordProvider> providerMap, Vertx vertx) {
        this.providerMap = providerMap;
        if (vertx == null) {
            throw new RuntimeException("Vert.x instance cannot be null");
        }
        this.vertx = vertx;
    }

    public void reloadDnsProviderCredentials(List<DnsConfig> dnsConfigList) {
        for (DnsConfig config : dnsConfigList) {
            if (config.getId() != null && config.getSecret() != null) {
                final var provider = providerMap.get(config.getDnsProviderType());
                if (provider != null) {
                    provider.reloadCredentials(config.getId(), config.getSecret());
                }
            }
        }
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

    protected Boolean createGenericsResultHandler(Object result) {
        if (result instanceof DnspodDataResult dnspodDataResult) {
            final var response = dnspodDataResult.getResponse();
            return response.getError() == null && response.getRecordId() != null;
        } else if (result instanceof CloudflareDataResult cloudflareDataResult) {
            return cloudflareDataResult.getSuccess()
                    && CollectionUtil.isEmpty(cloudflareDataResult.getErrors())
                    && !CollectionUtil.isEmpty(cloudflareDataResult.getResult());
        } else if (result instanceof AliyunDataResult aliyunDataResult) {
            return aliyunDataResult.getMessage() == null && aliyunDataResult.getRecordId() != null;
        } else if (result instanceof HuaweiDataResult huaweiDataResult) {
            return huaweiDataResult.getMessage() == null && !CollectionUtil.isEmpty(huaweiDataResult.getRecordsets());
        }
        return Boolean.FALSE;
    }

    protected Boolean modifyGenericsResultHandler(Object result) {
        if (result instanceof DnspodDataResult dnspodDataResult) {
            final var response = dnspodDataResult.getResponse();
            return response.getRecordId() != null && response.getError() == null;
        } else if (result instanceof CloudflareDataResult cloudflareDataResult){
            return cloudflareDataResult.getSuccess()
                    && CollectionUtil.isEmpty(cloudflareDataResult.getErrors())
                    && !CollectionUtil.isEmpty(cloudflareDataResult.getResult());
        } else if (result instanceof HuaweiDataResult huaweiDataResult) {
            return huaweiDataResult.getMessage() == null && !CollectionUtil.isEmpty(huaweiDataResult.getRecordsets());
        } else if (result instanceof AliyunDataResult aliyunDataResult) {
            return aliyunDataResult.getMessage() == null && aliyunDataResult.getRecordId() != null;
        }
        return Boolean.FALSE;
    }

    protected Boolean deleteGenericsResultHandler(Object result) {
        if (result instanceof DnspodDataResult dnspodDataResult) {
            final var response = dnspodDataResult.getResponse();
            return response.getError() == null && response.getRecordId() != null;
        } else if (result instanceof CloudflareDataResult cloudflareDataResult) {
            return cloudflareDataResult.getSuccess()
                    && !CollectionUtil.isEmpty(cloudflareDataResult.getResult())
                    && CollectionUtil.isEmpty(cloudflareDataResult.getErrors());
        } else if (result instanceof AliyunDataResult aliyunDataResult) {
            return aliyunDataResult.getMessage() == null && aliyunDataResult.getRecordId() != null;
        } else if (result instanceof HuaweiDataResult huaweiDataResult) {
            return huaweiDataResult.getMessage() == null && !CollectionUtil.isEmpty(huaweiDataResult.getRecordsets());
        }
        return Boolean.FALSE;
    }

    protected List<DnsRecordVo> findGenericsResultHandler(Object result, String domain) {
        List<DnsRecordVo> recordVoList = new LinkedList<>();
        if (result instanceof AliyunDataResult aliyunDataResult && aliyunDataResult.getDomainRecords() != null) {
            final var domainRecords = aliyunDataResult.getDomainRecords()
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

        if (result instanceof DnspodDataResult dnspodDataResult && dnspodDataResult.getResponse() != null) {
            final var recordList = dnspodDataResult.getResponse()
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

        if (result instanceof HuaweiDataResult huaweiDataResult) {
            final var recordList = huaweiDataResult.getRecordsets();
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

        if (result instanceof CloudflareDataResult cloudflareDataResult && cloudflareDataResult.getSuccess()) {
            final var resultList = cloudflareDataResult.getResult();
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
