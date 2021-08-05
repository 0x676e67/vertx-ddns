package com.zf1976.ddns.api;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.util.Assert;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.ObjectUtil;
import com.zf1976.ddns.util.StringUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author mac
 * @date 2021/7/18
 */
@SuppressWarnings("deprecation")
public abstract class AbstractDnsApi<T, A> implements DnsRecordApi<T> {

    protected final Logger log = LogManager.getLogger("[AbstractDnsApi]");
    protected final DnsApiCredentials dnsApiCredentials;
    protected final HttpClient httpClient = HttpClient.newBuilder()
                                                      .connectTimeout(Duration.ofSeconds(5))
                                                      .executor(Executors.newSingleThreadExecutor())
                                                      .build();
    protected final WebClient webClient;

    protected AbstractDnsApi(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
        vertx = vertx != null ? vertx : Vertx.vertx();
        Assert.notNull(dnsApiCredentials, "Credentials cannot been null!");
        this.dnsApiCredentials = dnsApiCredentials;
        final var webClientOptions = new WebClientOptions()
                .setSsl(true);
        this.webClient = WebClient.create(vertx, webClientOptions);
    }

    protected void checkIp(String ip) {
        if (!HttpUtil.isIp(ip)) {
            throw new RuntimeException("ip：" + ip + " unqualified");
        }
    }

    protected void checkDomain(String domain) {
        if (HttpUtil.isDomain(domain)) {
            throw new RuntimeException("domain：" + domain + " unqualified");
        }
    }

    /**
     * 获取反序列化的集合类型JavaType
     *
     * @param clazz 元素类型
     * @return {@link JavaType}
     */
    protected static JavaType getListType(Class<?> clazz) {
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
    protected static JavaType getMapType(Class<?> keyType, Class<?> valueType) {
        return MapType.construct(HashMap.class, SimpleType.constructUnsafe(keyType), SimpleType.constructUnsafe(valueType));
    }

    protected <E> E mapperResult(byte[] bytes, Class<E> tClass) {
        try {
            if (ObjectUtil.isEmpty(bytes)) {
                return null;
            }
            return Json.decodeValue(Buffer.buffer(bytes), tClass);
        } catch (DecodeException e) {
            log.error(e.getMessage(), e.getCause());
            return null;
        }
    }

    protected <E> E mapperResult(String content, Class<E> tClass) {
        if (StringUtil.isEmpty(content)) {
            return null;
        }
        return this.mapperResult(content.getBytes(StandardCharsets.UTF_8), tClass);
    }

    protected String concatUrl(String first, String... more) {
        final var urlBuilder = new StringBuilder(first);
        for (String path : more) {
            urlBuilder.append("/")
                      .append(path);
        }
        return urlBuilder.toString();
    }

    protected Map<String, Object> getQueryParam(String recordId, String domain, A action) {
        return this.getQueryParam(recordId, domain, null, null, action);
    }

    protected Map<String, Object> getQueryParam(String domain, DNSRecordType dnsRecordType, A action) {
        return this.getQueryParam(null, domain, null, dnsRecordType, action);
    }

    protected Map<String, Object> getQueryParam(String domain, String ip, DNSRecordType dnsRecordType, A action) {
        return this.getQueryParam(null, domain, ip, dnsRecordType, action);
    }

    protected Map<String, Object> getQueryParam(String recordId,
                                                String domain,
                                                String ip,
                                                DNSRecordType dnsRecordType,
                                                A action) {

        return null;
    }
}