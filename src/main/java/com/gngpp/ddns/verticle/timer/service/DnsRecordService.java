/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.verticle.timer.service;

import com.gngpp.ddns.pojo.vo.DnsRecord;
import com.gngpp.ddns.config.DnsConfig;
import com.gngpp.ddns.enums.DnsProviderType;
import com.gngpp.ddns.enums.DnsRecordType;
import com.gngpp.ddns.verticle.timer.DnsRecordObserver;
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
