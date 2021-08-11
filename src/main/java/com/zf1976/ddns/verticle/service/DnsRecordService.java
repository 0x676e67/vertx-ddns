package com.zf1976.ddns.verticle.service;

import com.zf1976.ddns.api.enums.DnsProviderType;
import com.zf1976.ddns.api.enums.DnsRecordType;
import com.zf1976.ddns.pojo.vo.DnsRecordVo;
import io.vertx.core.Future;

import java.util.List;

/**
 * @author mac
 * 2021/8/11 星期三 9:02 下午
 */
public interface DnsRecordService {

    List<DnsRecordVo> findRecordList(DnsProviderType dnsServiceType, String domain, DnsRecordType dnsRecordType);

    Boolean createRecord(DnsProviderType dnsProviderType, String domain, String ip, DnsRecordType dnsRecordType);

    Boolean modifyRecord(DnsProviderType dnsProviderType,
                         String id,
                         String domain,
                         String ip,
                         DnsRecordType dnsRecordType);

    Boolean deleteRecord(DnsProviderType dnsProviderType, String recordId, String domain);

    Future<List<DnsRecordVo>> findRecordListAsync(DnsProviderType dnsServiceType,
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

}
