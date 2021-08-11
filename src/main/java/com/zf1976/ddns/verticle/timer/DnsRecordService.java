package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.enums.DnsRecordType;
import com.zf1976.ddns.api.provider.DnsRecordProvider;
import com.zf1976.ddns.api.provider.exception.FoundDnsProviderException;
import com.zf1976.ddns.api.provider.exception.NotSupportDnsProviderException;
import com.zf1976.ddns.pojo.*;
import com.zf1976.ddns.pojo.vo.DnsRecordVo;
import com.zf1976.ddns.api.enums.DnsProviderType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author ant
 * Create by Ant on 2021/7/28 10:01 下午
 */
public class DnsRecordService extends AbstractDnsRecordService {

    private final Logger log = LogManager.getLogger("[DnsRecordService]");

    public DnsRecordService(List<DnsConfig> ddnsConfigList, Vertx vertx) {
        super(ddnsConfigList, vertx);
    }

    public List<DnsRecordVo> findRecords(DnsProviderType dnsServiceType, String domain, DnsRecordType dnsRecordType) {
        final var provider = this.providerMap.get(dnsServiceType);
        this.checkProvider(provider, dnsServiceType);
        final var result = provider.findDnsRecordList(domain, dnsRecordType);
        return super.findGenericsResultHandler(result, domain);
    }


    public Boolean createRecord(DnsProviderType dnsProviderType, String domain, String ip, DnsRecordType dnsRecordType) {
        final var provider = this.providerMap.get(dnsProviderType);
        this.checkProvider(provider, dnsProviderType);
        final var result = provider.createDnsRecord(domain, ip, dnsRecordType);
        return super.createGenericsResultHandler(result);
    }

    public Boolean modifyRecord(DnsProviderType dnsProviderType, String id, String domain, String ip, DnsRecordType dnsRecordType) {
        final var provider = this.providerMap.get(dnsProviderType);
        this.checkProvider(provider, dnsProviderType);
        final var result = provider.modifyDnsRecord(id, domain, ip, dnsRecordType);
        return super.modifyGenericsResultHandler(result);
    }

    public Boolean deleteRecord(DnsProviderType dnsProviderType, String recordId, String domain) {
        final var provider = this.providerMap.get(dnsProviderType);
        this.checkProvider(provider, dnsProviderType);
        final var resultObj = provider.deleteDnsRecord(recordId, domain);
        return super.deleteGenericsResultHandler(resultObj);
    }

    @SuppressWarnings("unchecked")
    public Future<List<DnsRecordVo>> findRecordListAsync(DnsProviderType dnsServiceType,
                                                         String domain,
                                                         DnsRecordType dnsRecordType) {
        return Future.succeededFuture(this.providerMap.get(dnsServiceType))
                     .compose(provider -> {
                         if (provider == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return provider.supportAsync(dnsServiceType)
                                        .compose(v -> provider.findDnsRecordListAsync(domain, dnsRecordType))
                                        .compose(result -> {
                                            final var dnsRecordVoList = this.findGenericsResultHandler(result, domain);
                                            return Future.succeededFuture(dnsRecordVoList);
                                        });
                     });
    }

    @SuppressWarnings("unchecked")
    public Future<Boolean> createRecordAsync(DnsProviderType dnsProviderType, String domain, String ip, DnsRecordType dnsRecordType) {
        return Future.succeededFuture(this.providerMap.get(dnsProviderType))
                     .compose(provider -> {
                         if (provider == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return provider.supportAsync(dnsProviderType)
                                        .compose(v -> provider.createDnsRecordAsync(domain, ip, dnsRecordType))
                                        .compose(v -> Future.succeededFuture(super.createGenericsResultHandler(v)));
                     });
    }

    @SuppressWarnings("unchecked")
    public Future<Boolean> modifyRecordAsync(DnsProviderType dnsProviderType, String id, String domain, String ip, DnsRecordType dnsRecordType) {
        return Future.succeededFuture(this.providerMap.get(dnsProviderType))
                     .compose(provider -> {
                         if (provider == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return provider.supportAsync(dnsProviderType)
                                        .compose(v -> provider.modifyDnsRecordAsync(id, domain, ip, dnsRecordType))
                                        .compose(v -> Future.succeededFuture(super.modifyGenericsResultHandler(v)));
                     });
    }

    @SuppressWarnings("unchecked")
    public Future<Boolean> deleteRecordAsync(DnsProviderType dnsProviderType, String id, String domain) {
        return Future.succeededFuture(this.providerMap.get(dnsProviderType))
                     .compose(provider -> {
                         if (provider == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return provider.supportAsync(dnsProviderType)
                                        .compose(v -> provider.deleteDnsRecordAsync(id, domain))
                                        .compose(v -> Future.succeededFuture(super.deleteGenericsResultHandler(v)));
                     });

    }

    private void checkProvider(DnsRecordProvider<?> provider, DnsProviderType dnsProviderType) {
        if (provider == null) {
            throw new FoundDnsProviderException("No service provider");
        } else if (!provider.support(dnsProviderType)) {
            throw new NotSupportDnsProviderException("The :" + dnsProviderType.name() + " DNS service provider is not supported");
        }
    }

}
