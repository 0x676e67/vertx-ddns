package com.zf1976.ddns.verticle.timer;

import com.zf1976.ddns.api.enums.DnsRecordType;
import com.zf1976.ddns.api.provider.AliyunDnsProvider;
import com.zf1976.ddns.api.provider.CloudflareDnsProvider;
import com.zf1976.ddns.api.provider.DnspodDnsProvider;
import com.zf1976.ddns.api.provider.HuaweiDnsProvider;
import com.zf1976.ddns.api.provider.DnsRecordProvider;
import com.zf1976.ddns.api.provider.exception.FoundDnsProviderException;
import com.zf1976.ddns.api.provider.exception.NotSupportDnsProviderException;
import com.zf1976.ddns.pojo.*;
import com.zf1976.ddns.pojo.vo.DnsRecordVo;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
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
@SuppressWarnings("rawtypes")
public class DnsRecordService extends AbstractDnsRecordHandler{

    private final Logger log = LogManager.getLogger("[DnsConfigTimerService]");

    public DnsRecordService(List<DnsConfig> ddnsConfigList, Vertx vertx) {
        super(ddnsConfigList, vertx);
    }

    public List<DnsRecordVo> findDnsRecords(DnsProviderType dnsServiceType, String domain, DnsRecordType dnsRecordType) {
        final var api = this.dnsApiMap.get(dnsServiceType);
        if (api == null) {
            throw new FoundDnsProviderException("No service provider");
        } else if (!api.support(dnsServiceType)) {
            throw new NotSupportDnsProviderException("The :" + dnsServiceType.name() + " DNS service provider is not supported");
        }
        final var result = api.findDnsRecordList(domain, dnsRecordType);
        return this.findGenericsResultHandler(result, domain);
    }

    @SuppressWarnings("unchecked")
    public Future<List<DnsRecordVo>> findDnsRecordListAsync(DnsProviderType dnsServiceType,
                                                            String domain,
                                                            DnsRecordType dnsRecordType) {
        return Future.succeededFuture(this.dnsApiMap.get(dnsServiceType))
                     .compose(api -> {
                         if (api == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return api.supportAsync(dnsServiceType)
                                   .compose(v -> api.findDnsRecordListAsync(domain, dnsRecordType))
                                   .compose(result -> {
                                       final var dnsRecordVoList = this.findGenericsResultHandler(result, domain);
                                       return Future.succeededFuture(dnsRecordVoList);
                                   });
                     });


    }

    public Boolean deleteRecord(DnsProviderType dnsProviderType, String recordId, String domain) {
        final var api = this.dnsApiMap.get(dnsProviderType);
        if (api == null) {
            throw new FoundDnsProviderException("No service provider");
        } else if (!api.support(dnsProviderType)) {
            throw new NotSupportDnsProviderException("The :" + dnsProviderType.name() + " DNS service provider is not supported");
        }

        final var resultObj = api.deleteDnsRecord(recordId, domain);
        return this.deleteResultGenericsResultHandler(resultObj);
    }

    @SuppressWarnings("unchecked")
    public Future<Boolean> deleteRecordAsync(DnsProviderType dnsProviderType, String recordId, String domain) {
        final var api = this.dnsApiMap.get(dnsProviderType);
        return Future.succeededFuture(api)
                     .compose(checkApi -> {
                         if (checkApi == null) {
                             return Future.failedFuture("No service provider");
                         }
                         return checkApi.supportAsync(dnsProviderType)
                                        .compose(v -> checkApi.deleteDnsRecordAsync(recordId, domain))
                                        .compose(v -> Future.succeededFuture(this.deleteResultGenericsResultHandler(v)));
                     });

    }


}
