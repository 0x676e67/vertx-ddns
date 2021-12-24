/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
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

package com.zf1976.ddns.api.provider;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.ProviderCredentials;
import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.enums.DnsRecordType;
import io.vertx.core.Future;

/**
 * @author ant
 * Create by Ant on 2021/7/29 1:46 上午
 */
@SuppressWarnings("unused")
public interface DnsRecordProvider<T> {

    /**
     * 具体参数作用请看实现类注释
     *
     * @param domain        域名
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T findDnsRecordList(String domain, DnsRecordType dnsRecordType);

    /**
     * 具体参数作用请看实现类注释
     *
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T createDnsRecord(String domain, String ip, DnsRecordType dnsRecordType);

    /**
     * 具体参数作用请看实现类注释
     *
     * @param id            记录唯一凭证
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T modifyDnsRecord(String id, String domain, String ip, DnsRecordType dnsRecordType);

    /**
     * 具体参数作用请看实现类注释
     *
     * @param id     记录唯一凭证
     * @param domain 域名
     * @return {@link T}
     */
    T deleteDnsRecord(String id, String domain);


    /**
     * 异步版本
     *
     * @param domain        域名
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    default Future<T> findDnsRecordListAsync(String domain, DnsRecordType dnsRecordType) {
        throw new UnsupportedOperationException();
    }

    /**
     * 异步版本
     *
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link Future<T>}
     */
    default Future<T> createDnsRecordAsync(String domain, String ip, DnsRecordType dnsRecordType) {
        throw new UnsupportedOperationException();
    }

    /**
     * 异步版本
     *
     * @param id            id
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link Future<T>}
     */
    default Future<T> modifyDnsRecordAsync(String id, String domain, String ip, DnsRecordType dnsRecordType) {
        throw new UnsupportedOperationException();
    }

    /**
     * 异步版本
     *
     * @param id     id
     * @param domain 域名
     * @return {@link Future<T>}
     */
    default Future<T> deleteDnsRecordAsync(String id, String domain) {
        throw new UnsupportedOperationException();
    }

    /**
     * 某些使用zone区域划分域名记录的DNS服务商，需强迫使用supports
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    boolean support(DnsProviderType dnsServiceType);

    /**
     * 异步版本
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link Future<Boolean>}
     */
    Future<Boolean> supportAsync(DnsProviderType dnsServiceType);

    /**
     * 重新加载凭证
     *
     * @param dnsProviderCredentials provider credentials
     */
    void reloadCredentials(ProviderCredentials dnsProviderCredentials);

    /**
     * 功能描述
     *
     * @param accessKey accessKey
     * @param secretKey secretKey
     */
    default void reloadCredentials(String accessKey, String secretKey) {
        this.reloadCredentials(new BasicCredentials(accessKey, secretKey));
    }
}
