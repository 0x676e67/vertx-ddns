package com.zf1976.ddns.verticle.timer.service;

import com.zf1976.ddns.api.enums.DnsRecordType;
import com.zf1976.ddns.api.provider.AliyunDnsProvider;
import com.zf1976.ddns.api.provider.CloudflareDnsProvider;
import com.zf1976.ddns.api.provider.DnspodDnsProvider;
import com.zf1976.ddns.api.provider.HuaweiDnsProvider;
import com.zf1976.ddns.api.provider.DnsRecordProvider;
import com.zf1976.ddns.pojo.*;
import com.zf1976.ddns.api.enums.DnsProviderType;
import com.zf1976.ddns.pojo.vo.DnsRecord;
import com.zf1976.ddns.util.*;
import com.zf1976.ddns.verticle.ApiConstants;
import com.zf1976.ddns.verticle.handler.ResolveDnsRecordHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ant
 * Create by Ant on 2021/8/7 2:19 PM
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractDnsRecordService implements ResolveDnsRecordHandler, DnsRecordService {

    protected final Logger log = LogManager.getLogger("[AbstractDnsRecordService]");
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

    protected List<DnsRecord> findGenericsResultHandler(Object result, String domain) {
        List<DnsRecord> recordVoList = new LinkedList<>();
        if (result instanceof AliyunDataResult aliyunDataResult && aliyunDataResult.getDomainRecords() != null) {
            final var domainRecords = aliyunDataResult.getDomainRecords()
                                            .getRecordList();
            for (AliyunDataResult.Record record : domainRecords) {
                final var recordVo = DnsRecord.newBuilder()
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
                    final var recordVo = DnsRecord.newBuilder()
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
                    final var recordVo = DnsRecord.newBuilder()
                                                  .withId(record.getId())
                                                  .withDomain(extractDomain[0])
                                                  .withRr(Objects.equals(extractDomain[1], "")? "@" : extractDomain[1])
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
                    final var recordVo = DnsRecord.newBuilder()
                                                  .withId(record.getId())
                                                  .withDomain(record.getZoneName())
                                                  .withRr(Objects.equals(extractDomain[1], "")? "@" : extractDomain[1])
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
                this.parserDnsRecordForIpv4(config.getDnsProviderType(), config.getIpv4Config());
                this.parserDnsRecordForIpv6(config.getDnsProviderType(), config.getIpv6Config());
            }
        }
    }

    @Override
    public void parserDnsRecordForIpv4(DnsProviderType dnsProviderType, DnsConfig.Ipv4Config ipv4Config) {
        if (Objects.nonNull(ipv4Config) && ipv4Config.getEnable()) {
            // use ip api or network
            Future.succeededFuture(ipv4Config.getSelectIpMethod())
                  .compose(bool -> {
                      // get ip from api
                      if (bool) {
                          return HttpUtil.getCurrentHostIpv4(ipv4Config.getInputIpApi(), vertx.getOrCreateContext());
                      } else {
                          // get ip from network
                          return Future.succeededFuture(ipv4Config.getNetworkIp());
                      }
                  })
                  .onSuccess(resultIp -> this.parserDnsRecordForIpv4Handler(dnsProviderType, ipv4Config, resultIp))
                  .onFailure(err -> log.error(err.getMessage(), err.getCause()));
        }
    }


    @Override
    public void parserDnsRecordForIpv6(DnsProviderType dnsProviderType, DnsConfig.Ipv6Config ipv6Config) {
        if (Objects.nonNull(ipv6Config) && ipv6Config.getEnable()) {
            // use ip api or network
            Future.succeededFuture(ipv6Config.getSelectIpMethod())
                    .compose(bool -> {
                        // get ip from api
                        if (bool) {
                            return HttpUtil.getCurrentHostIpv4(ipv6Config.getInputIpApi(), vertx.getOrCreateContext());
                        } else {
                            return Future.succeededFuture(ipv6Config.getNetworkIp());
                        }
                    })
                  .onSuccess(resultIp -> this.parserDnsRecordForIpv6Handler(dnsProviderType, ipv6Config, resultIp))

                  .onFailure(err -> log.error(err.getMessage(), err.getCause()));
        }
    }

    private void parserDnsRecordForIpv4Handler(DnsProviderType dnsProviderType, DnsConfig.Ipv4Config ipv4Config, String resultIp) {
        @SuppressWarnings("DuplicatedCode") String defaultIp = StringUtil.isEmpty(resultIp)? "" : resultIp;
        final var domainList = ipv4Config.getDomainList();
        for (String domainAndIp : domainList) {

            final var extractDomainAndIp = this.extractDomainAndIp(domainAndIp, defaultIp);
            if (!ObjectUtil.isEmpty(extractDomainAndIp)) {
                final String domain = extractDomainAndIp[0];
                final String ip = extractDomainAndIp[1];
                this.findRecordListAsync(dnsProviderType, domain, DnsRecordType.A)
                    .compose(recordList -> this.validateDnsRecordStatus(recordList, dnsProviderType, domain, ip, DnsRecordType.A))
                    .onSuccess(message -> {
                        LogUtil.printDebug(this.log, message);
                        vertx.eventBus().send(ApiConstants.CONFIG_SUBJECT_ADDRESS, message);
                    })
                    .onFailure(err -> {
                        LogUtil.printDebug(this.log, err.getMessage(), err.getCause());
                        vertx.eventBus().send(ApiConstants.CONFIG_SUBJECT_ADDRESS, err.getMessage());
                    });
            }

        }
    }

    private void parserDnsRecordForIpv6Handler(DnsProviderType dnsProviderType, DnsConfig.Ipv6Config ipv6Config, String resultIp) {
        String defaultIp = StringUtil.isEmpty(resultIp)? "" : resultIp;
        final var domainList = ipv6Config.getDomainList();
        for (String domainAndIp : domainList) {

            final var extractDomainAndIp = this.extractDomainAndIp(domainAndIp, defaultIp);
            if (!ObjectUtil.isEmpty(extractDomainAndIp)) {
                final String domain = extractDomainAndIp[0];
                final String ip = extractDomainAndIp[1];
                this.findRecordListAsync(dnsProviderType, domain, DnsRecordType.AAAA)
                    .compose(recordList -> this.validateDnsRecordStatus(recordList, dnsProviderType, domain, ip, DnsRecordType.AAAA))
                    .onSuccess(message -> {
                        LogUtil.printDebug(this.log, message);
                        vertx.eventBus().send(ApiConstants.CONFIG_SUBJECT_ADDRESS, message);
                    })
                    .onFailure(err -> {
                        LogUtil.printDebug(this.log, err.getMessage(), err.getCause());
                        vertx.eventBus().send(ApiConstants.CONFIG_SUBJECT_ADDRESS, err.getMessage());
                    });
            }

        }
    }

    /**
     * 提取 域名:IP [0]--domain    [1]--ip
     *
     * @param domainAndIp 域名和ip组合 -> www.baidu.com:1.1.1.1
     * @param defaultIp 默认ip
     * @return {@link String[]}
     */
    private String[] extractDomainAndIp(String domainAndIp, String defaultIp) {
        // empty
        if (StringUtil.isEmpty(domainAndIp) || StringUtil.isEmpty(defaultIp)) {
            return new String[0];
        }
        final String domain;
        final String ip;
        final var splitDomainAndIp = domainAndIp.split(":", 2);
        domain = splitDomainAndIp[0];
        if (splitDomainAndIp.length < 2) {
            if (StringUtil.isEmpty(domain)) {
                return new String[0];
            }
            ip = defaultIp;
        } else {
            ip = splitDomainAndIp[1];
        }
        return new String[]{domain, ip};
    }

    private String concatDomain(DnsRecord dnsRecord) {
        return Objects.equals(dnsRecord.getRr(), "@") ? dnsRecord.getDomain() : dnsRecord.getRr() + "." + dnsRecord.getDomain();
    }

    Future<String> validateDnsRecordStatus(List<DnsRecord> recordList, DnsProviderType dnsProviderType, String domain, String ip, DnsRecordType dnsRecordType) {
        final var domainMap = recordList.stream()
                                        .collect(Collectors.toMap(this::concatDomain, DnsRecord::getId));
        // find if the domain name exists
        final var id = domainMap.get(domain);
        // The domain name does not exist, create a domain name record resolution
        if (StringUtil.isEmpty(id)) {
            return this.createRecordAsync(dnsProviderType, domain, ip, dnsRecordType)
                       .compose(bool -> bool? Future.succeededFuture("[" + dnsProviderType.name() + "] 新增域名解析：" + domain + " 成功！IP：" + ip)
                               : Future.succeededFuture("[" + dnsProviderType.name() + "] 新增域名解析：" + domain + " 失败！"));
        } else {
            for (DnsRecord dnsRecord : recordList) {
                final var concatDomain = this.concatDomain(dnsRecord);
                // If the domain name resolution record exists,
                // if the ip is changed, the domain name record resolution will be updated
                if (Objects.equals(concatDomain, domain) && !Objects.equals(dnsRecord.getValue(), ip)) {
                    return this.modifyRecordAsync(dnsProviderType, id, domain, ip, dnsRecordType)
                               .compose(bool -> bool? Future.succeededFuture("[" + dnsProviderType.name() + "] 更新域名解析：" + domain + " 成功！IP：" + ip)
                                       : Future.succeededFuture("[" + dnsProviderType.name() + "] 更新域名解析：" + domain + " 失败！"));
                }
            }
            return Future.succeededFuture("[" + dnsProviderType.name() + "] 域名：" + domain + " 没有发生变化, IP：" + ip);
        }
    }

}
