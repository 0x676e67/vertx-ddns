package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.enums.DnsSRecordType;
import com.zf1976.ddns.api.provider.AliyunDnsRecordProvider;
import com.zf1976.ddns.api.provider.CloudflareDnsRecordProvider;
import com.zf1976.ddns.api.provider.DnspodDnsRecordProvider;
import com.zf1976.ddns.api.provider.HuaweiDnsProvider;
import com.zf1976.ddns.api.provider.DnsRecordProvider;
import com.zf1976.ddns.api.provider.exception.DnsServiceResponseException;
import com.zf1976.ddns.api.provider.exception.FoundDnsProviderException;
import com.zf1976.ddns.api.provider.exception.NotSupportDnsProviderException;
import com.zf1976.ddns.pojo.*;
import com.zf1976.ddns.pojo.vo.DnsRecordVo;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.LogUtil;
import com.zf1976.ddns.verticle.DnsServiceType;
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
public class DnsConfigTimerService extends AbstractDnsRecordHandler{

    private final Logger log = LogManager.getLogger("[DnsConfigTimerService]");

    public DnsConfigTimerService(List<DnsConfig> ddnsConfigList, Vertx vertx) {
        super(ddnsConfigList, vertx);
    }

    public List<DnsRecordVo> findDnsRecords(DnsServiceType dnsServiceType, String domain, DnsSRecordType dnsRecordType) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        if (api == null) {
            throw new FoundDnsProviderException("No service provider");
        } else if (!api.support(dnsServiceType)) {
            throw new NotSupportDnsProviderException("The :" + dnsServiceType.name() + "DNS service provider is not supported");
        }
        final var result = api.findDnsRecordList(domain, dnsRecordType);
        return this.handlerGenericsResult(result, domain);
    }

    @SuppressWarnings("unchecked")
    public Future<List<DnsRecordVo>> findDnsRecordListAsync(DnsServiceType dnsServiceType,
                                                            String domain,
                                                            DnsSRecordType dnsRecordType) {
        return Future.succeededFuture(this.dnsApiMap.get(dnsServiceType))
                     .compose(api -> {
                         if (api == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return api.supportAsync(dnsServiceType)
                                   .compose(v -> api.findDnsRecordListAsync(domain, dnsRecordType));
                     })
                     .compose(result -> {
                         final var dnsRecordVoList = this.handlerGenericsResult(result, domain);
                         return Future.succeededFuture(dnsRecordVoList);
                     });

    }

    public Boolean deleteRecord(DnsServiceType dnsServiceType, String recordId, String domain) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        if (api == null) {
            throw new FoundDnsProviderException("No service provider");
        } else if (!api.support(dnsServiceType)) {
            throw new NotSupportDnsProviderException("The :" + dnsServiceType.name() + "DNS service provider is not supported");
        }

        if (api instanceof DnspodDnsRecordProvider) {
            final var dnspodDataResult = (DnspodDataResult) api.deleteDnsRecord(recordId, domain);
            return dnspodDataResult != null && dnspodDataResult.getResponse()
                                                               .getError() == null;
        } else if (api instanceof AliyunDnsRecordProvider || api instanceof HuaweiDnsProvider) {
            return api.deleteDnsRecord(recordId, domain) != null;
        } else if (api instanceof CloudflareDnsRecordProvider) {
            return ((CloudflareDataResult) api.deleteDnsRecord(recordId, domain)).getSuccess();
        }

        return Boolean.FALSE;
    }

    @SuppressWarnings("unchecked")
    public Future<Boolean> deleteRecordAsync(DnsServiceType dnsServiceType, String recordId, String domain) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        return Future.succeededFuture(api)
                     .compose(checkApi -> {
                         if (checkApi == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return checkApi.supportAsync(dnsServiceType)
                                        .compose(v -> checkApi.deleteDnsRecordAsync(recordId, domain));
                     })
                     .compose(result -> this.futureDeleteResultHandler(api, result));

    }

    private Future<Boolean> futureDeleteResultHandler(DnsRecordProvider api, Object result) {
        boolean complete = Boolean.FALSE;
        if (api instanceof DnspodDnsRecordProvider) {
            final var dnspodDataResult = (DnspodDataResult) result;
            complete = dnspodDataResult != null && dnspodDataResult.getResponse()
                                                                   .getError() == null;
        } else if (api instanceof AliyunDnsRecordProvider || api instanceof HuaweiDnsProvider) {
            complete = result != null;
        } else if (api instanceof CloudflareDnsRecordProvider) {
            complete = ((CloudflareDataResult) result).getSuccess();
        }
        return Future.succeededFuture(complete);
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
