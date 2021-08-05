package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.auth.TokenCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.pojo.CloudflareDataResult;
import com.zf1976.ddns.pojo.CloudflareDataResult.Result;
import com.zf1976.ddns.util.Assert;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * cloudflare DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:24 上午
 */
public class CloudflareDnsAPI extends AbstractDnsAPI<CloudflareDataResult, Object> {

    private final Logger log = LogManager.getLogger("[CloudflareDnsAPI]");
    private final String api = "https://api.cloudflare.com/client/v4/zones/";
    private final Map<String, String> zoneMap = new HashMap<>();

    public CloudflareDnsAPI(String token, Vertx vertx) {
        this(new TokenCredentials(token), vertx);
    }


    public CloudflareDnsAPI(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
        super(dnsApiCredentials, vertx);
        final var request = HttpRequest.newBuilder()
                                       .GET()
                                       .uri(URI.create(api))
                                       .header("Authorization", "Bearer " + dnsApiCredentials.getAccessKeySecret())
                                       .build();
        try {
            final var body = super.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                             .body();
            final var cloudflareDataResult = this.mapperResult(body, CloudflareDataResult.class);
            Assert.notNull(cloudflareDataResult, "result cannot been null");
            if (!cloudflareDataResult.getSuccess()) {
                throw new RuntimeException(Json.encodePrettily(cloudflareDataResult.getErrors()));
            }
            final var result = cloudflareDataResult.getResult();
            // 按域名分区映射区域id
            for (Result res : result) {
                this.zoneMap.put(res.getName(), res.getId());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * 查询所有记录
     *
     * @param domain  域名/不区分主域名、多级域名（相当于查询主域名下所有记录）
     * @param dnsRecordType 记录类型
     * @return {@link CloudflareDataResult}
     */
    public CloudflareDataResult findDnsRecords(String domain, DNSRecordType dnsRecordType) {
        final var queryParam = getQueryParam(dnsRecordType);
        final var url = this.toUrl(domain, queryParam);
        final var request = this.requestBuild(url, MethodType.GET);
        return this.sendRequest(request);
    }

    /**
     * 新增记录
     *
     * @param domain        域名/区分主域名与多级域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link CloudflareDataResult}
     */
    public CloudflareDataResult addDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        this.checkIp(ip);
        final var queryParam = this.getQueryParam(dnsRecordType);
        queryParam.put("name", domain);
        queryParam.put("content", ip);
        queryParam.put("ttl", "120");
        final var url = toUrl(domain, queryParam, null);
        final var httpRequest = this.requestBuild(url, queryParam, MethodType.POST);
        return this.sendRequest(httpRequest);
    }

    /**
     * 更新记录值
     *
     * @param identifier    记录标识符
     * @param domain        域名
     * @param ip            ip值
     * @param dnsRecordType 记录类型
     * @return {@link CloudflareDataResult}
     */
    public CloudflareDataResult updateDnsRecord(String identifier,
                                                        String domain,
                                                        String ip,
                                                        DNSRecordType dnsRecordType) {
        this.checkIp(ip);
        this.checkDomain(domain);
        final var queryParam = this.getQueryParam(dnsRecordType);
        queryParam.put("name", domain);
        queryParam.put("content", ip);
        queryParam.put("ttl", "120");
        final var url = this.toUrl(domain, queryParam, identifier);
        final var httpRequest = this.requestBuild(url, queryParam, MethodType.PUT);
        return this.sendRequest(httpRequest);
    }

    /**
     * 删除记录
     *
     * @param identifier 记录标识符
     * @param domain     域名/不区分顶级域名、多级域名
     * @return {@link CloudflareDataResult}
     */
    public CloudflareDataResult deleteDnsRecord(String identifier, String domain) {
        final var url = this.toUrl(domain, identifier);
        final var httpRequest = this.requestBuild(url, MethodType.DELETE);
        return this.sendRequest(httpRequest);
    }

    /**
     * 是否支持
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    @Override
    public boolean supports(DNSServiceType dnsServiceType) {
        return DNSServiceType.CLOUDFLARE.check(dnsServiceType);
    }

    private CloudflareDataResult sendRequest(HttpRequest request) {
        try {
            final var body = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                       .body();
            final var jsonObject = JsonObject.mapFrom(Json.decodeValue(body));
            final var resultKey = "result";
            final var result = jsonObject.getMap()
                                         .get(resultKey);
            if (result instanceof ArrayList) {
                return this.mapperResult(body, CloudflareDataResult.class);
            } else {
                jsonObject.put(resultKey, Collections.singletonList(result));
                return jsonObject.mapTo(CloudflareDataResult.class);
            }
        } catch (IOException | InterruptedException | DecodeException e) {
            log.error(e.getMessage(), e.getCause());

        }
        return null;
    }

    private HttpRequest requestBuild(String url, MethodType methodType) {
        return this.requestBuild(url, null, methodType);
    }

    private HttpRequest requestBuild(String url, Object data, MethodType methodType) {
        final var builder = HttpRequest.newBuilder();
        builder.uri(URI.create(url))
               .header(HttpHeaders.AUTHORIZATION, this.getBearerToken())
               .header(HttpHeaders.CONTENT_TYPE, "application/json");
        switch (methodType) {
            case GET:
                builder.GET();
                break;
            case PUT:
                builder.PUT(HttpRequest.BodyPublishers.ofString(Json.encode(data)));
                break;
            case POST:
                builder.POST(HttpRequest.BodyPublishers.ofString(Json.encode(data)));
                break;
            case DELETE:
                builder.DELETE();
                break;
            default:
        }
        return builder.build();
    }

    private String getBearerToken() {
        final var token = super.dnsApiCredentials.getAccessKeySecret();
        return "Bearer " + token;
    }

    private String toUrl(String domain, String identifier) {
        return this.toUrl(domain, null, identifier);
    }

    private String toUrl(String domain, Map<String, String> queryParam) {
        return this.toUrl(domain, queryParam, null);
    }

    private String toUrl(String domain, Map<String, String> queryParam, String identifier) {
        if (!CollectionUtil.isEmpty(queryParam)) {
            final var query = new StringBuilder();
            final var array = queryParam.keySet()
                                        .toArray(new String[]{});
            for (String key : array) {
                query.append("&")
                     .append(key)
                     .append("=")
                     .append(queryParam.get(key));
            }
            return this.getZoneUrl(domain) + (StringUtil.isEmpty(identifier) ? "?" : "/" + identifier + "?") + query.substring(1);
        }
        return this.getZoneUrl(domain) + (StringUtil.isEmpty(identifier) ? "?" : "/" + identifier);
    }

    private String getZoneUrl(String domain) {
        final var extractDomain = HttpUtil.extractDomain(domain);
        // 如果域名属于二级及以上域名，则根据cloudflare查询策略，按主域名查询
        final var zoneId = this.zoneMap.get(extractDomain[0]);
        if (StringUtil.isEmpty(zoneId)) {
            throw new RuntimeException("Resolved primary domain name:" + domain + "does not exist");
        }
        return this.concatUrl(this.api, zoneId, "dns_records");
    }

    private Map<String, String> getQueryParam(DNSRecordType dnsRecordType) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("match", "all");
        queryParam.put("type", dnsRecordType.name());
        queryParam.put("per_page", "100");
        return queryParam;
    }

}
