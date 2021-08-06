package com.zf1976.ddns.api.impl;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.pojo.HuaweiDataResult;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.LogUtil;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 华为DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:25 上午
 */
@SuppressWarnings({"SpellCheckingInspection"})
public class HuaweiDnsApi extends AbstractDnsApi<HuaweiDataResult, HuaweiDnsApi.Action> {

    private final Logger log = LogManager.getLogger("[HuaweiDnsApi]");
    private final String api = "https://dns.myhuaweicloud.com/v2/zones";
    private final CloseableHttpClient closeableHttpClient = HttpClients.custom()
                                                                       .build();
    private final Map<String, String> zoneMap = new ConcurrentHashMap<>();

    public HuaweiDnsApi(String id, String secret, Vertx vertx) {
        this(new BasicCredentials(id, secret), vertx);
    }

    public HuaweiDnsApi(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
        super(dnsApiCredentials, vertx);
    }

    private void initZoneMap() {
        if (!CollectionUtil.isEmpty(this.zoneMap)) {
            return;
        }
        try {
            final var httpRequestBase = HuaweiRequest.newBuilder(dnsApiCredentials)
                                                     .setUrl(this.api)
                                                     .setMethod(MethodType.GET)
                                                     .build();
            final var contentBytes = this.sendRequest(httpRequestBase);
            final var huaweiDataResult = this.mapperResult(contentBytes, HuaweiDataResult.class);
            final var zones = huaweiDataResult.getZones();
            if (!CollectionUtil.isEmpty(zones)) {
                // 按域名划分区域
                for (HuaweiDataResult.Zones zone : zones) {
                    var domain = zone.getName();
                    domain = domain.substring(0, domain.length() - 1);
                    this.zoneMap.put(domain, zone.getId());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Future<Void> asyncInitZoneMap() {
        if (!CollectionUtil.isEmpty(this.zoneMap)) {
            return Future.succeededFuture();
        }
        return Future.failedFuture(new RuntimeException());
    }

    /**
     * 查找记录列表
     *
     * @param domain     域名/不区分顶级域名、多级域名
     * @param recordType 记录类型
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult findDnsRecordList(String domain, DNSRecordType recordType) {
        final var httpRequestBase = HuaweiRequest.newBuilder(this.dnsApiCredentials)
                                                 .setUrl(this.getZoneUrl(domain))
                                                 .addQueryStringParam("type", recordType.name())
                                                 .setMethod(MethodType.GET)
                                                 .build();
        return this.sendRequest(httpRequestBase, Action.DESCRIBE);
    }

    /**
     * 新增记录
     *
     * @param domain     域名/不区分顶级域名、多级域名
     * @param ip         ip
     * @param recordType 记录类型
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult createDnsRecord(String domain, String ip, DNSRecordType recordType) {
        final var jsonObject = new JsonObject().put("name", domain + ".")
                                               .put("type", recordType.name())
                                               .put("records", Collections.singletonList(ip));
        final var httpRequestBase = HuaweiRequest.newBuilder(this.dnsApiCredentials)
                                                 .setUrl(this.getZoneUrl(domain))
                                                 .setMethod(MethodType.POST)
                                                 .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                                 .setBody(jsonObject.encode())
                                                 .build();
        return this.sendRequest(httpRequestBase, Action.CREATE);
    }

    /**
     * 更新记录
     *
     * @param reocrdSetId 记录唯一id
     * @param domain      域名/不区分顶级域名、多级域名
     * @param ip          ip
     * @param recordType  记录类型
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult modifyDnsRecord(String reocrdSetId, String domain, String ip, DNSRecordType recordType) {
        final var jsonObject = new JsonObject().put("type", recordType.name())
                                               .put("name", domain + ".")
                                               .put("records", Collections.singletonList(ip));
        final var httpRequestBase = HuaweiRequest.newBuilder(this.dnsApiCredentials)
                                                 .setUrl(this.getZoneUrl(domain, reocrdSetId))
                                                 .setMethod(MethodType.PUT)
                                                 .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                                 .setBody(jsonObject.encode())
                                                 .build();
        return this.sendRequest(httpRequestBase, Action.MODIFY);
    }

    /**
     * 删除记录
     *
     * @param recordSetId 记录唯一id
     * @param domain 域名/不区分顶级域名、多级域名
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult deleteDnsRecord(String recordSetId, String domain) {
        final var httpRequestBase = HuaweiRequest.newBuilder(this.dnsApiCredentials)
                                                 .setUrl(this.getZoneUrl(domain, recordSetId))
                                                 .setMethod(MethodType.DELETE)
                                                 .build();
        return this.sendRequest(httpRequestBase, Action.DELETE);
    }

    /**
     * 是否支持
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    @Override
    public boolean supports(DNSServiceType dnsServiceType) {
        this.initZoneMap();
        return DNSServiceType.HUAWEI.check(dnsServiceType);
    }

    /**
     * 异步版本
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link Future <Boolean>}
     */
    @Override
    public Future<Boolean> asyncSupports(DNSServiceType dnsServiceType) {
        return null;
    }

    @Override
    HuaweiDataResult resultHandler(String body) {
        return null;
    }

    @Override
    Future<HuaweiDataResult> futureResultHandler(HttpResponse<Buffer> responseFuture) {
        return null;
    }

    @Override
    Future<HttpResponse<Buffer>> sendAsyncRequest(String url, JsonObject data, MethodType methodType) {
        return null;
    }

    private HuaweiDataResult resultToList(HuaweiDataResult.Recordsets result) {
        if (result != null) {
            return new HuaweiDataResult().setRecordsets(Collections.singletonList(result));
        }
        return null;
    }


    private byte[] sendRequest(HttpRequestBase httpRequestBase) {
        try (CloseableHttpResponse httpResponse = this.closeableHttpClient.execute(httpRequestBase)) {
            if (httpResponse != null) {
                return this.getContentBytes(httpResponse);
            }
        } catch (Exception e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
        }
        return new byte[0];
    }

    private HuaweiDataResult sendRequest(HttpRequestBase httpRequestBase, Action action) {
        final var bytes = this.sendRequest(httpRequestBase);
        final HuaweiDataResult huaweiDataResult;
        switch (action) {
            case DESCRIBE -> huaweiDataResult = this.mapperResult(bytes, HuaweiDataResult.class);
            case CREATE, MODIFY, DELETE -> {
                final var result = this.mapperResult(bytes, HuaweiDataResult.Recordsets.class);
                huaweiDataResult = this.resultToList(result);
            }
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }
        return huaweiDataResult;
    }


    private byte[] getContentBytes(CloseableHttpResponse httpResponse) throws IOException {
        final var content = httpResponse.getEntity()
                                        .getContent();
        final var statusCode = httpResponse.getStatusLine()
                                           .getStatusCode();
        if (statusCode == 200 || statusCode == 202 || statusCode == 204) {
            return content.readAllBytes();
        } else {
            LogUtil.printDebug(log, Json.decodeValue(Buffer.buffer(content.readAllBytes())));
        }
        return new byte[0];
    }

    private String getZoneUrl(String domain) {
        return this.getZoneUrl(domain, null);
    }

    private String getZoneUrl(String domain, String recordSetId) {
        final var extractDomain = HttpUtil.extractDomain(domain);
        String zoneId = this.zoneMap.get(extractDomain[0]);
        if (StringUtil.isEmpty(zoneId)) {
            throw new RuntimeException("Resolved primary domain name:" + domain + "does not exist");
        }
        if (StringUtil.isEmpty(recordSetId)) {
            return this.concatUrl(this.api, zoneId, "recordsets");
        }
        return this.concatUrl(this.api, zoneId, "recordsets", recordSetId);
    }


    protected enum Action {
        DESCRIBE,
        CREATE,
        DELETE,
        MODIFY
    }
}
