package com.zf1976.ddns.api;

import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Future;

/**
 * @author ant
 * Create by Ant on 2021/7/29 1:46 上午
 */
public interface DnsRecordAPI<T> {

    /**
     * 具体参数作用请看实现类注释
     *
     * @param domain        域名
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T findDnsRecords(String domain, DNSRecordType dnsRecordType);

    /**
     * 具体参数作用请看实现类注释
     *
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T addDnsRecord(String domain, String ip, DNSRecordType dnsRecordType);

    /**
     * 具体参数作用请看实现类注释
     *
     * @param id            记录唯一凭证
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link T}
     */
    T updateDnsRecord(String id, String domain, String ip, DNSRecordType dnsRecordType);

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
    default Future<T> asyncFindDnsRecords(String domain, DNSRecordType dnsRecordType) {
        return null;
    }

    /**
     * 异步版本
     *
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link Future<T>}
     */
    default Future<T> asyncAddDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        return null;
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
    default Future<T> asyncUpdateDnsRecord(String id, String domain, String ip, DNSRecordType dnsRecordType) {
        return null;
    }

    /**
     * 异步版本
     *
     * @param id     id
     * @param domain 域名
     * @return {@link Future<T>}
     */
    default Future<T> asyncDeleteDnsRecord(String id, String domain) {
        return null;
    }

    /**
     * 是否支持
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    boolean supports(DNSServiceType dnsServiceType);
}
