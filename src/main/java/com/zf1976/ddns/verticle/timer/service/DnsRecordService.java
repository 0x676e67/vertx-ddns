package com.zf1976.ddns.verticle.timer.service;

import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.enums.DnsRecordType;
import com.zf1976.ddns.pojo.DnsConfig;
import com.zf1976.ddns.pojo.vo.DnsRecord;
import com.zf1976.ddns.verticle.timer.DnsRecordObserver;
import io.vertx.core.Future;

import java.util.List;

/**
 * @author mac
 * 2021/8/11 星期三 9:02 下午
 */
public interface DnsRecordService extends DnsRecordObserver {

    List<DnsRecord> findRecordList(DnsProviderType dnsServiceType, String domain, DnsRecordType dnsRecordType);

    Boolean createRecord(DnsProviderType dnsProviderType, String domain, String ip, DnsRecordType dnsRecordType);

    Boolean modifyRecord(DnsProviderType dnsProviderType,
                         String id,
                         String domain,
                         String ip,
                         DnsRecordType dnsRecordType);

    Boolean deleteRecord(DnsProviderType dnsProviderType, String recordId, String domain);

    Future<List<DnsRecord>> findRecordListAsync(DnsProviderType dnsServiceType,
                                                String domain,
                                                DnsRecordType dnsRecordType);

    Future<Boolean> createRecordAsync(DnsProviderType dnsProviderType,
                                      String domain,
                                      String ip,
                                      DnsRecordType dnsRecordType);

    Future<Boolean> modifyRecordAsync(DnsProviderType dnsProviderType,
                                      String id,
                                      String domain,
                                      String ip,
                                      DnsRecordType dnsRecordType);

    Future<Boolean> deleteRecordAsync(DnsProviderType dnsProviderType, String recordId, String domain);

    void reloadProviderCredentials(List<DnsConfig> dnsConfigList);
}
