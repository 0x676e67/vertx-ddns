package com.zf1976.ddns.api.provider;

import com.zf1976.ddns.api.enums.DnsSRecordType;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Future;

/**
 * @author ant
 * Create by Ant on 2021/7/29 1:46 上午
 */
public interface DnsRecordProvider<T> {

    /**
     * 具体参数作用请看实现类注释
     *
     * @param domain        域名
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T findDnsRecordList(String domain, DnsSRecordType dnsRecordType);

    /**
     * 具体参数作用请看实现类注释
     *
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T createDnsRecord(String domain, String ip, DnsSRecordType dnsRecordType);

    /**
     * 具体参数作用请看实现类注释
     *
     * @param id            记录唯一凭证
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T modifyDnsRecord(String id, String domain, String ip, DnsSRecordType dnsRecordType);

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
    default Future<T> asyncFindDnsRecordList(String domain, DnsSRecordType dnsRecordType) {
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
    default Future<T> asyncCreateDnsRecord(String domain, String ip, DnsSRecordType dnsRecordType) {
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
    default Future<T> asyncModifyDnsRecord(String id, String domain, String ip, DnsSRecordType dnsRecordType) {
        throw new UnsupportedOperationException();
    }

    /**
     * 异步版本
     *
     * @param id     id
     * @param domain 域名
     * @return {@link Future<T>}
     */
    default Future<T> asyncDeleteDnsRecord(String id, String domain) {
        throw new UnsupportedOperationException();
    }

    /**
     * 某些使用zone区域划分域名记录的DNS服务商，需强迫使用supports
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    boolean support(DNSServiceType dnsServiceType) throws Exception;

    /**
     * 异步版本
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link Future<Boolean>}
     */
    Future<Boolean> supportAsync(DNSServiceType dnsServiceType);
}