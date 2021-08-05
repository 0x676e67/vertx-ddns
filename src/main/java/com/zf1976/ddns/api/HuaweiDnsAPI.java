package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.pojo.HuaweiDataResult;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 华为DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:25 上午
 */
@SuppressWarnings({"SpellCheckingInspection"})
public class HuaweiDnsAPI extends AbstractDnsAPI<HuaweiDataResult, Object> {

    private final Logger log = LogManager.getLogger("[HuaweiDnsAPI]");
    private final String api = "https://dns.myhuaweicloud.com/v2/zones";
    private final CloseableHttpClient closeableHttpClient = HttpClients.custom()
                                                                       .build();
    private final Map<String, String> zoneMap = new HashMap<>();

    public HuaweiDnsAPI(String id, String secret, Vertx vertx) {
        this(new BasicCredentials(id, secret), vertx);
    }

    public HuaweiDnsAPI(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
        super(dnsApiCredentials, vertx);
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

    /**
     * 查找记录列表
     *
     * @param domain 域名/不区分顶级域名、多级域名
     * @param recordType 记录类型
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult findDnsRecords(String domain, DNSRecordType recordType) {
        final var httpRequestBase = this.getRequestBuilder()
                .setUrl(this.getZoneUrl(domain))
                .addQueryStringParam("type", recordType.name())
                .setMethod(MethodType.GET)
                .build();
        byte[] contentBytes = this.sendRequest(httpRequestBase);
        return this.mapperResult(contentBytes, HuaweiDataResult.class);
    }

    /**
     * 新增记录
     *
     * @param domain 域名/不区分顶级域名、多级域名
     * @param ip ip
     * @param recordType 记录类型
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult addDnsRecord(String domain, String ip, DNSRecordType recordType) {
        final var jsonObject = new JsonObject().put("name", domain + ".")
                                               .put("type", recordType.name())
                                               .put("records", Collections.singletonList(ip));
        final var httpRequestBase = this.getRequestBuilder()
                                        .setUrl(this.getZoneUrl(domain))
                                        .setMethod(MethodType.POST)
                                        .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                        .setBody(jsonObject.encode())
                                        .build();
        final var contentBytes = this.sendRequest(httpRequestBase);
        final var result = this.mapperResult(contentBytes, HuaweiDataResult.Recordsets.class);
        return this.resultToList(result);
    }

    /**
     * 更新记录
     *
     * @param reocrdSetId 记录唯一id
     * @param domain 域名/不区分顶级域名、多级域名
     * @param ip ip
     * @param recordType 记录类型
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult updateDnsRecord(String reocrdSetId,String domain, String ip, DNSRecordType recordType) {
        final var jsonObject = new JsonObject().put("type", recordType.name())
                                               .put("name", domain + ".")
                                               .put("records", Collections.singletonList(ip));
        final var httpRequestBase = this.getRequestBuilder()
                              .setUrl(this.getZoneUrl(domain, reocrdSetId))
                              .setMethod(MethodType.PUT)
                              .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                              .setBody(jsonObject.encode())
                              .build();
        final var contentBytes = this.sendRequest(httpRequestBase);
        final var result = this.mapperResult(contentBytes, HuaweiDataResult.Recordsets.class);
        return this.resultToList(result);
    }

    /**
     * 删除记录
     *
     * @param recordSetId 记录唯一id
     * @param domain 域名/不区分顶级域名、多级域名
     * @return {@link HuaweiDataResult}
     */
    public HuaweiDataResult deleteDnsRecord(String recordSetId, String domain) {
        final var httpRequestBase = this.getRequestBuilder()
                                        .setUrl(this.getZoneUrl(domain, recordSetId))
                                        .setMethod(MethodType.DELETE)
                                        .build();
        final var contentBytes = this.sendRequest(httpRequestBase);
        final var result = this.mapperResult(contentBytes, HuaweiDataResult.Recordsets.class);
        return this.resultToList(result);
    }

    /**
     * 是否支持
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    @Override
    public boolean supports(DNSServiceType dnsServiceType) {
        return DNSServiceType.HUAWEI.check(dnsServiceType);
    }

    private HuaweiDataResult resultToList(HuaweiDataResult.Recordsets result) {
        if (result != null) {
            return new HuaweiDataResult().setRecordsets(Collections.singletonList(result));
        }
        return null;
    }

    private byte[] sendRequest(HttpRequestBase httpRequestBase) {
        try (CloseableHttpResponse httpResponse = this.executeRequest(httpRequestBase)) {
            if (httpResponse != null) {
                return this.getContentBytes(httpResponse);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
        }
        return null;
    }

    private CloseableHttpResponse executeRequest(HttpRequestBase httpRequestBase) {
        try {
            return this.closeableHttpClient.execute(httpRequestBase);
        } catch (IOException e) {
            log.error(e.getMessage(), e.getCause());
        }
        return null;
    }

    private byte[] getContentBytes(CloseableHttpResponse httpResponse) throws IOException {
        final var content = httpResponse.getEntity().getContent();
        final var statusCode = httpResponse.getStatusLine()
                                           .getStatusCode();
        if (statusCode == 200 || statusCode ==202 || statusCode == 204) {
            return content.readAllBytes();
        } else {
            log.warn(Json.decodeValue(Buffer.buffer(content.readAllBytes())));
        }
        return null;
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

    private HuaweiRequest getRequestBuilder() {
        return HuaweiRequest.newBuilder(this.dnsApiCredentials);
    }
}
