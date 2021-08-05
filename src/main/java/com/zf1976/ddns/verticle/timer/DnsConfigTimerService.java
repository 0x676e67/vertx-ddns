package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.*;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.pojo.*;
import com.zf1976.ddns.pojo.vo.DnsRecordVo;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author ant
 * Create by Ant on 2021/7/28 10:01 下午
 */
@SuppressWarnings("rawtypes")
public class DnsConfigTimerService {

    private final Logger log = LogManager.getLogger("[DnsConfigTimerService]");
    private final Map<DNSServiceType, DnsRecordAPI> dnsApiMap;

    public DnsConfigTimerService(List<DDNSConfig> ddnsConfigList, Vertx vertx) {
        this(new HashMap<>(4));
        for (DDNSConfig config : ddnsConfigList) {
            if (config.getId() != null && config.getSecret() != null) {
                switch (config.getDnsServiceType()) {
                    case ALIYUN -> dnsApiMap.put(DNSServiceType.ALIYUN, new AliyunDnsAPI(config.getId(), config.getSecret(), vertx));
                    case DNSPOD -> dnsApiMap.put(DNSServiceType.DNSPOD, new DnspodDnsAPI(config.getId(), config.getSecret(), vertx));
                    case HUAWEI -> dnsApiMap.put(DNSServiceType.HUAWEI, new HuaweiDnsAPI(config.getId(), config.getSecret(), vertx));
                    case CLOUDFLARE -> dnsApiMap.put(DNSServiceType.CLOUDFLARE, new CloudflareDnsAPI(config.getSecret(), vertx));
                }
            }
        }
    }

    public DnsConfigTimerService(Map<DNSServiceType, DnsRecordAPI> dnsApiMap) {
        this.dnsApiMap = dnsApiMap;
    }

    public List<DnsRecordVo> findDnsRecords(DNSServiceType dnsServiceType, String domain, DNSRecordType dnsRecordType) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        if (api != null && api.supports(dnsServiceType)) {
            try {
                final var result = api.findDnsRecords(domain, dnsRecordType);
                return this.handlerGenericsResult(result, domain);
            } catch (Exception e) {
                log.error(e.getMessage(), e.getCause());
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }
        throw new RuntimeException("No service provider key configured");
    }

    @SuppressWarnings("unchecked")
    public Future<List<DnsRecordVo>> asyncFindDnsRecords(DNSServiceType dnsServiceType,
                                                         String domain,
                                                         DNSRecordType dnsRecordType) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        if (api != null && api.supports(dnsServiceType)) {
            return api.asyncFindDnsRecords(domain, dnsRecordType)
                      .compose(result -> {
                          final var dnsRecordVoList = this.handlerGenericsResult(result, domain);
                          return Future.succeededFuture(dnsRecordVoList);
                      });
        }
        return Future.failedFuture("No service provider key configured");
    }

    public Boolean deleteRecord(DNSServiceType dnsServiceType, String recordId, String domain) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        if (api != null && api.supports(dnsServiceType)) {
            try {
                if (api instanceof DnspodDnsAPI) {
                    final var dnspodDataResult = (DnspodDataResult) api.deleteDnsRecord(recordId, domain);
                    return dnspodDataResult != null && dnspodDataResult.getResponse()
                                                                       .getError() == null;
                }
                if (api instanceof AliyunDnsAPI || api instanceof HuaweiDnsAPI) {
                    return api.deleteDnsRecord(recordId, domain) != null;
                }
                if (api instanceof CloudflareDnsAPI) {
                    return ((CloudflareDataResult) api.deleteDnsRecord(recordId, domain)).getSuccess();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e.getCause());
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("No service provider key configured");
    }

    @SuppressWarnings("unchecked")
    public Future<Boolean> asyncDeleteRecord(DNSServiceType dnsServiceType, String recordId, String domain) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        if (api != null && api.supports(dnsServiceType)) {
            return api.asyncDeleteDnsRecord(recordId, domain)
                      .compose(result -> {
                          Boolean complete = Boolean.FALSE;
                          if (api instanceof DnspodDnsAPI) {
                              final var dnspodDataResult = (DnspodDataResult) result;
                              complete = dnspodDataResult != null && dnspodDataResult.getResponse()
                                                                                     .getError() == null;
                          }
                          if (api instanceof AliyunDnsAPI || api instanceof HuaweiDnsAPI) {
                              complete = result != null;
                          }
                          if (api instanceof CloudflareDnsAPI) {
                              complete = ((CloudflareDataResult) result).getSuccess();
                          }
                          return Future.succeededFuture(complete);
                      });
        }
        return Future.failedFuture("No service provider key configured");
    }

    private List<DnsRecordVo> handlerGenericsResult(Object obj, String domain) {
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
