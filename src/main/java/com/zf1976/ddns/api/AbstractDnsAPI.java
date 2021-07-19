package com.zf1976.ddns.api;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.util.HttpUtil;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;

/**
 * @author mac
 * @date 2021/7/18
 */
public class AbstractDnsAPI {

    protected final DnsApiCredentials dnsApiCredentials;

    protected AbstractDnsAPI(DnsApiCredentials dnsApiCredentials) {
        this.dnsApiCredentials = dnsApiCredentials;
    }

    protected HttpClient httpClient = HttpClient.newBuilder()
                                                .connectTimeout(Duration.ofSeconds(2))
                                                .version(HttpClient.Version.HTTP_1_1)
                                                .executor(Executors.newSingleThreadExecutor())
                                                .build();

    public void checkIp(String ip) {
        if (!HttpUtil.isIp(ip)) {
            throw new RuntimeException("ip：" + ip + " unqualified");
        }
    }

    public void checkDomain(String domain) {
        if (!HttpUtil.isDomain(domain)) {
            throw new RuntimeException("domain：" + domain + " unqualified");
        }
    }

    /**
     * 获取反序列化的集合类型JavaType
     *
     * @param clazz 元素类型
     * @return {@link JavaType}
     */
    public static JavaType getListType(Class<?> clazz) {
        return CollectionType
                .construct(LinkedList.class, SimpleType.construct(clazz));
    }

    /**
     * 获取反序列化的map类型JavaType
     *
     * @param keyType   键类型
     * @param valueType 值类型
     * @return {@link JavaType}
     */
    public static JavaType getMapType(Class<?> keyType, Class<?> valueType) {
        return MapType.construct(HashMap.class, SimpleType.constructUnsafe(keyType), SimpleType.constructUnsafe(valueType));
    }

}
