package com.zf1976.ddns.api.impl;

import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.auth.TokenCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.HttpMethod;
import com.zf1976.ddns.pojo.CloudflareDataResult;
import com.zf1976.ddns.pojo.CloudflareDataResult.Result;
import com.zf1976.ddns.util.*;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * cloudflare DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:24 上午
 */
@SuppressWarnings("RedundantCast")
public class CloudflareDnsApi extends AbstractDnsApi<CloudflareDataResult, CloudflareDnsApi.Action> {

    private final Logger log = LogManager.getLogger("[CloudflareDnsApi]");
    private final String api = "https://api.cloudflare.com/client/v4/zones";
    private final Map<String, String> zoneMap = new ConcurrentHashMap<>();

    public CloudflareDnsApi(String token, Vertx vertx) {
        this(new TokenCredentials(token), vertx);
    }


    public CloudflareDnsApi(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
        super(dnsApiCredentials, vertx);
    }

    private void initZoneMap() throws Exception {
        if (!CollectionUtil.isEmpty(this.zoneMap)) {
            return;
        }
        final var request = HttpRequest.newBuilder()
                                       .GET()
                                       .uri(URI.create(api))
                                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + dnsApiCredentials.getAccessKeySecret())
                                       .build();
        final var body = super.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                         .body();
        final var result = this.extractZoneResult(body);
        // 按域名分区映射区域id
        for (Result res : result) {
            this.zoneMap.put(res.getName(), res.getId());
        }
    }

    private Future<Void> asyncInitZoneMap() {
        if (!CollectionUtil.isEmpty(this.zoneMap)) {
            return Future.succeededFuture();
        }
        return this.webClient.getAbs(this.api)
                             .putHeader(HttpHeaders.AUTHORIZATION, this.bearerToken())
                             .send()
                             .compose(v -> {
                                 final var body = v.bodyAsString();
                                 try {
                                     final var result = this.extractZoneResult(body);
                                     // 按域名分区映射区域id
                                     for (Result res : result) {
                                         this.zoneMap.put(res.getName(), res.getId());
                                     }
                                     return Future.succeededFuture();
                                 } catch (Exception e) {
                                     return Future.failedFuture(e.getMessage());
                                 }
                             });
    }

    private List<CloudflareDataResult.Result> extractZoneResult(String body) {
        final var cloudflareDataResult = this.mapperResult(body, CloudflareDataResult.class);
        Assert.notNull(cloudflareDataResult, "result cannot been null");
        if (!cloudflareDataResult.getSuccess()) {
            throw new RuntimeException(Json.encodePrettily(cloudflareDataResult.getErrors()));
        }
        return cloudflareDataResult.getResult();
    }

    /**
     * 查询所有记录
     *
     * @param domain        域名/不区分主域名、多级域名（相当于查询主域名下所有记录）
     * @param dnsRecordType 记录类型
     * @return {@link CloudflareDataResult}
     */
    public CloudflareDataResult findDnsRecordList(String domain, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(domain, dnsRecordType, Action.DESCRIBE);
        final var request = this.requestBuild(domain, queryParam, HttpMethod.GET);
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
    public CloudflareDataResult createDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        final var data = this.getQueryParam(domain, ip, dnsRecordType, Action.CREATE);
        final var httpRequest = this.requestBuild(domain, data, HttpMethod.POST);
        return this.sendRequest(httpRequest);
    }

    /**
     * 更新记录值
     *
     * @param id            记录标识符
     * @param domain        域名
     * @param ip            ip值
     * @param dnsRecordType 记录类型
     * @return {@link CloudflareDataResult}
     */
    public CloudflareDataResult modifyDnsRecord(String id,
                                                String domain,
                                                String ip,
                                                DNSRecordType dnsRecordType) {
        final var data = this.getQueryParam(id, domain, ip, dnsRecordType, Action.MODIFY);
        final var httpRequest = this.requestBuild(id, domain, data, HttpMethod.PUT);
        return this.sendRequest(httpRequest);
    }

    /**
     * 删除记录
     *
     * @param id     记录标识符
     * @param domain 域名/不区分顶级域名、多级域名
     * @return {@link CloudflareDataResult}
     */
    public CloudflareDataResult deleteDnsRecord(String id, String domain) {
        final var httpRequest = this.requestBuild(id, domain, HttpMethod.DELETE);
        return this.sendRequest(httpRequest);
    }

    /**
     * 异步版本
     *
     * @param domain        域名
     * @param dnsRecordType 记录类型
     * @return {@link Future<CloudflareDataResult>}
     */
    @Override
    public Future<CloudflareDataResult> asyncFindDnsRecordList(String domain, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(domain, dnsRecordType, Action.DESCRIBE);
        final var url = this.requestUrlBuild(domain, queryParam);
        return this.sendAsyncRequest(url, HttpMethod.GET)
                   .compose(this::futureResultHandler);
    }

    /**
     * 异步版本
     *
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link Future<CloudflareDataResult>}
     */
    @Override
    public Future<CloudflareDataResult> asyncCreateDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        final var data = this.getQueryParam(domain, ip, dnsRecordType, Action.CREATE);
        final var url = this.requestUrlBuild(domain);
        return this.sendAsyncRequest(url, JsonObject.mapFrom(data), HttpMethod.POST)
                   .compose(this::futureResultHandler);
    }

    /**
     * 异步版本
     *
     * @param id            id
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link Future<CloudflareDataResult>}
     */
    @Override
    public Future<CloudflareDataResult> asyncModifyDnsRecord(String id,
                                                             String domain,
                                                             String ip,
                                                             DNSRecordType dnsRecordType) {
        final var data = this.getQueryParam(id, domain, ip, dnsRecordType, Action.MODIFY);
        final var url = this.requestUrlBuild(id, domain);
        return this.sendAsyncRequest(url, JsonObject.mapFrom(data), HttpMethod.PUT)
                   .compose(this::futureResultHandler);
    }

    /**
     * 异步版本
     *
     * @param id     id
     * @param domain 域名
     * @return {@link Future<CloudflareDataResult>}
     */
    @Override
    public Future<CloudflareDataResult> asyncDeleteDnsRecord(String id, String domain) {
        final var url = this.requestUrlBuild(id, domain);
        return this.sendAsyncRequest(url, HttpMethod.DELETE)
                   .compose(this::futureResultHandler);
    }

    /**
     * 是否支持
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    @Override
    public boolean support(DNSServiceType dnsServiceType) throws Exception {
        this.initZoneMap();
        return DNSServiceType.CLOUDFLARE.check(dnsServiceType);
    }

    /**
     * 使用api之前调用该函数进行判断，是否支持使用
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link Future<Boolean>}
     */
    @Override
    public Future<Boolean> supportAsync(DNSServiceType dnsServiceType) {
        return this.asyncInitZoneMap()
                   .compose(v -> Future.succeededFuture(!CollectionUtil.isEmpty(this.zoneMap) && DNSServiceType.CLOUDFLARE.check(dnsServiceType)));
    }

    private String bearerToken() {
        final var token = super.dnsApiCredentials.getAccessKeySecret();
        return "Bearer " + token;
    }


    private HttpRequest requestBuild(String domain, Map<String, Object> queryParam, HttpMethod methodType) {
        return this.requestBuild((String) null, domain, queryParam, methodType);
    }


    private HttpRequest requestBuild(String id,
                                     String domain,
                                     @SuppressWarnings("SameParameterValue") HttpMethod methodType) {
        return this.requestBuild(id, domain, (Map<String, Object>) null, methodType);
    }

    private HttpRequest requestBuild(String id, String domain, Map<String, Object> queryParam, HttpMethod methodType) {
        final String url;
        HttpRequest httpRequest;
        switch (methodType) {
            case GET -> {
                url = this.requestUrlBuild(domain, queryParam);
                httpRequest = this.request(url, methodType);
            }
            case POST -> {
                url = this.requestUrlBuild(domain);
                httpRequest = this.request(url, JsonObject.mapFrom(queryParam), methodType);
            }
            case PUT -> {
                url = this.requestUrlBuild(id, domain);
                httpRequest = this.request(url, JsonObject.mapFrom(queryParam), methodType);
            }
            case DELETE -> {
                url = this.requestUrlBuild(id, domain);
                httpRequest = this.request(url, methodType);
            }
            default -> throw new IllegalStateException("Unexpected value: " + methodType);
        }
        return httpRequest;
    }

    private HttpRequest request(String url, HttpMethod methodType) {
        return this.request(url, null, methodType);
    }

    private HttpRequest request(String url, JsonObject data, HttpMethod methodType) {
        final var builder = HttpRequest.newBuilder();
        builder.uri(URI.create(url))
               .header(HttpHeaders.AUTHORIZATION, this.bearerToken());
        switch (methodType) {
            case GET -> builder.GET();
            case PUT -> builder.PUT(HttpRequest.BodyPublishers.ofString(data.encode()))
                               .header(HttpHeaders.CONTENT_TYPE, "application/json");
            case POST -> builder.POST(HttpRequest.BodyPublishers.ofString(data.encode()))
                                .header(HttpHeaders.CONTENT_TYPE, "application/json");
            case DELETE -> builder.DELETE();
        }
        return builder.build();
    }

    private CloudflareDataResult sendRequest(HttpRequest request) {
        try {
            final var body = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                       .body();
            return this.resultHandler(body);
        } catch (IOException | InterruptedException e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
        }
        return null;
    }

    protected Future<io.vertx.ext.web.client.HttpResponse<Buffer>> sendAsyncRequest(String url, HttpMethod methodType) {
        return this.sendAsyncRequest(url, null, methodType);
    }


    protected Future<io.vertx.ext.web.client.HttpResponse<Buffer>> sendAsyncRequest(String url,
                                                                                    JsonObject data,
                                                                                    HttpMethod methodType) {
        final Future<io.vertx.ext.web.client.HttpResponse<Buffer>> httpResponseFuture;
        switch (methodType) {
            case GET -> httpResponseFuture = this.webClient.getAbs(url)
                                                           .putHeader(HttpHeaders.AUTHORIZATION, this.bearerToken())
                                                           .send();
            case POST -> httpResponseFuture = this.webClient.postAbs(url)
                                                            .putHeader(HttpHeaders.AUTHORIZATION, this.bearerToken())
                                                            .sendJsonObject(data);
            case PUT -> httpResponseFuture = this.webClient.putAbs(url)
                                                           .putHeader(HttpHeaders.AUTHORIZATION, this.bearerToken())
                                                           .sendJsonObject(data);
            case DELETE -> httpResponseFuture = this.webClient.deleteAbs(url)
                                                              .putHeader(HttpHeaders.AUTHORIZATION, this.bearerToken())
                                                              .send();
            default -> {
                return Future.failedFuture("Unexpected value:" + methodType);
            }
        }
        return httpResponseFuture;
    }

    private String requestUrlBuild(String domain) {
        return this.requestUrlBuild(domain, (Map<String, Object>) null);
    }

    private String requestUrlBuild(String identifier, String domain) {
        return this.requestUrlBuild(identifier, domain, (Map<String, Object>) null);
    }

    private String requestUrlBuild(String domain, Map<String, Object> queryParam) {
        return this.requestUrlBuild((String) null, domain, queryParam);
    }

    private String requestUrlBuild(String identifier, String domain, Map<String, Object> queryParam) {
        final var canonicalizeStringQueryString = this.toCanonicalizeStringQueryString(queryParam);
        return this.urlBuild(identifier, domain, canonicalizeStringQueryString);
    }

    private String urlBuild(String identifier, String domain, String canonicalizeQueryString) {
        final var zoneUrl = this.getZoneUrl(domain);
        if (StringUtil.isEmpty(canonicalizeQueryString)) {
            return this.concatUrl(zoneUrl, (StringUtil.isEmpty(identifier) ? "?" : StringUtil.FOLDER_SEPARATOR + identifier));
        }
        return this.concatUrl(zoneUrl, (StringUtil.isEmpty(identifier) ? "?" : StringUtil.FOLDER_SEPARATOR + identifier + "?"), canonicalizeQueryString);
    }

    private String toCanonicalizeStringQueryString(Map<String, Object> queryParam) {
        if (!CollectionUtil.isEmpty(queryParam)) {
            final var builder = new StringBuilder();
            final var array = queryParam.keySet()
                                        .toArray(new String[]{});
            for (String key : array) {
                builder.append("&")
                       .append(key)
                       .append("=")
                       .append(queryParam.get(key));
            }
            return builder.substring(1);
        }
        return null;
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

    protected Future<CloudflareDataResult> futureResultHandler(io.vertx.ext.web.client.HttpResponse<Buffer> responseFuture) {
        final var body = responseFuture.bodyAsString();
        final var cloudflareDataResult = this.resultHandler(body);
        return Future.succeededFuture(cloudflareDataResult);
    }

    protected CloudflareDataResult resultHandler(String body) {
        try {
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
        } catch (Exception e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
            return null;
        }
    }

    private Map<String, Object> getCommonQueryParam(DNSRecordType dnsRecordType) {
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("match", "all");
        queryParam.put("type", dnsRecordType.name());
        queryParam.put("per_page", "100");
        return queryParam;
    }

    @Override
    protected Map<String, Object> getQueryParam(String recordId,
                                                String domain,
                                                String ip,
                                                DNSRecordType dnsRecordType,
                                                Action action) {
        final var queryParam = this.getCommonQueryParam(dnsRecordType);
        switch (action) {
            case CREATE, MODIFY -> {
                queryParam.put("name", domain);
                queryParam.put("content", ip);
                queryParam.put("ttl", "120");
            }
            case DESCRIBE, DELETE -> {
            }
        }
        return queryParam;
    }

    protected enum Action {
        CREATE,
        MODIFY,
        DESCRIBE,
        DELETE
    }
}
