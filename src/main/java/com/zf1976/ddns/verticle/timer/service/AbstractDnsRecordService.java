package com.zf1976.ddns.verticle.timer.service;

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
import com.zf1976.ddns.verticle.handler.PeriodicHandler;
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
    private final List<DnsConfig> dnsConfigList = new LinkedList<>();

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
        this.dnsConfigList.addAll(dnsConfigList);
    }

    protected AbstractDnsRecordService(Map<DnsProviderType, DnsRecordProvider> providerMap, Vertx vertx) {
        this.providerMap = providerMap;
        if (vertx == null) {
            throw new RuntimeException("Vert.x instance cannot be null");
        }
        this.vertx = vertx;
    }

    public void reloadProviderCredentials(List<DnsConfig> dnsConfigList) {
        if (!CollectionUtil.isEmpty(this.dnsConfigList)) {
            this.dnsConfigList.clear();
        }
        for (DnsConfig config : dnsConfigList) {
            if (config.getId() != null && config.getSecret() != null) {
                final var provider = providerMap.get(config.getDnsProviderType());
                if (provider != null) {
                    provider.reloadCredentials(config.getId(), config.getSecret());
                }
                this.dnsConfigList.add(config);
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

    @Override
    public void update() {
        if (!CollectionUtil.isEmpty(this.dnsConfigList)) {
            for (DnsConfig config : dnsConfigList) {
                if (Objects.nonNull(config)) {
                    this.ipv4RecordHandler(config.getDnsProviderType(), config.getIpv4Config());
                    this.ipv6RecordHandler(config.getDnsProviderType(), config.getIpv6Config());
                }
            }
        }
    }

    private void ipv4RecordHandler(DnsProviderType dnsProviderType, DnsConfig.Ipv4Config ipv4Config) {
        if (Objects.nonNull(ipv4Config) && ipv4Config.getEnable()) {
            // use ip api or network
            final String ip;
            if (ipv4Config.getSelectIpMethod()) {
                final var ipApi = ipv4Config.getInputIpApi();
                ip = ipApi != null? HttpUtil.getCurrentHostIp(ipApi) : HttpUtil.getCurrentHostIp();
            } else {
                ip = ipv4Config.getNetworkIp();
            }
            for (String domain : ipv4Config.getDomainList()) {
                System.out.println(domain);
            }
            System.out.println(ip);
        }
    }

    private void ipv6RecordHandler(DnsProviderType dnsProviderType, DnsConfig.Ipv6Config ipv6Config) {
        if (Objects.nonNull(ipv6Config) && ipv6Config.getEnable()) {
            // use ip api or network
            final String ip;
            if (ipv6Config.getSelectIpMethod()) {
                final var ipApi = ipv6Config.getInputIpApi();
                ip = ipApi != null? HttpUtil.getCurrentHostIp(ipApi) : HttpUtil.getCurrentHostIp();
            } else {
                ip = ipv6Config.getNetworkIp();
            }
            for (String domain : ipv6Config.getDomainList()) {
                System.out.println(domain);
            }
            System.out.println(ip);
        }
    }
}
