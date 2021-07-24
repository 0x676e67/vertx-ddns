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
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * cloudflare DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:24 上午
 */
@SuppressWarnings({"FieldCanBeLocal", "DuplicatedCode"})
public class CloudflareDnsAPI extends AbstractDnsAPI {

    private final Logger log = LogManager.getLogger("[CloudflareDnsAPI]");
    private final String api = "https://api.cloudflare.com/client/v4/zones";
    private final Map<String, String> zoneMap;

    public CloudflareDnsAPI(String token) {
        this(new TokenCredentials(token));
    }


    public CloudflareDnsAPI(DnsApiCredentials dnsApiCredentials) {
        super(dnsApiCredentials);
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
            this.zoneMap = new HashMap<>();
            @SuppressWarnings("unchecked") final var zoneList = (List<LinkedHashMap<String, String>>) cloudflareDataResult.getResult();
            // 按域名分区映射区域id
            for (LinkedHashMap<String, String> stringStringLinkedHashMap : zoneList) {
                // domain-zoneId
                zoneMap.put(stringStringLinkedHashMap.get("name"), stringStringLinkedHashMap.get("id"));
            }
            Assert.notNull(zoneMap, "cloudflare zoneMap cannot been null!");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * 查询所有记录
     *
     * @param dnsRecordType 记录类型
     * @return {@link CloudflareDataResult<List<Result>>}
     */
    public CloudflareDataResult<List<Result>> findDnsRecords(String domain, DNSRecordType dnsRecordType) {
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
     * @return {@link CloudflareDataResult<Result>}
     */
    public CloudflareDataResult<Result> addDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
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
     * @return {@link CloudflareDataResult<Result>}
     */
    public CloudflareDataResult<Result> updateDnsRecord(String identifier,
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
     * @return {@link CloudflareDataResult< Result>}
     */
    public CloudflareDataResult<Result> deleteDnsRecord(String domain, String identifier) {
        final var url = this.toUrl(domain, identifier);
        final var httpRequest = this.requestBuild(url, MethodType.DELETE);
        return this.sendRequest(httpRequest);
    }

    @SuppressWarnings("unchecked")
    private <T> CloudflareDataResult<T> sendRequest(HttpRequest request) {
        try {
            final var body = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                       .body();
            final var cloudflareDataResult = Json.decodeValue(body, CloudflareDataResult.class);
            if (cloudflareDataResult.getResult() instanceof ArrayList<?> result) {
                List<Result> targetList = new ArrayList<>();
                for (Object obj : result) {
                    final var target = JsonObject.mapFrom(obj)
                                                 .mapTo(Result.class);
                    targetList.add(target);
                }
                return cloudflareDataResult.setResult(targetList);
            } else if (cloudflareDataResult.getResult() instanceof Result) {
                final var result = cloudflareDataResult.getResult();
                return cloudflareDataResult.setResult(JsonObject.mapFrom(result)
                                                                .mapTo(Result.class));
            }
            return cloudflareDataResult;
        } catch (IOException | InterruptedException e) {
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
               .header("Authorization", this.getBearerToken())
               .header("Content-type", "application/json");
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

    @SuppressWarnings("SameParameterValue")
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
        return this.api + "/" + zoneId + "/dns_records";
    }

    private Map<String, String> getQueryParam(DNSRecordType dnsRecordType) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("match", "all");
        queryParam.put("type", dnsRecordType.name());
        queryParam.put("per_page", "100");
        return queryParam;
    }

}
