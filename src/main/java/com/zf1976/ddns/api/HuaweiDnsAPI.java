package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.pojo.HuaweiDataResult;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.StringUtil;
import io.vertx.core.json.JsonObject;
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
public class HuaweiDnsAPI extends AbstractDnsAPI {

    private final Logger log = LogManager.getLogger("[HuaweiDnsAPI]");
    private final String api = "https://dns.myhuaweicloud.com/v2/zones";
    private final CloseableHttpClient closeableHttpClient = HttpClients.custom().build();
    private final Map<String, String> zoneMap = new HashMap<>();

    public HuaweiDnsAPI(String id, String secret) {
        this(new BasicCredentials(id, secret));
    }

    public HuaweiDnsAPI(DnsApiCredentials dnsApiCredentials) {
        super(dnsApiCredentials);
        try {
            final var httpRequestBase = HuaweiRequest.newBuilder(dnsApiCredentials)
                    .setUrl(api)
                    .setMethod(MethodType.GET)
                    .build();
            final var contentBytes = this.executeRequest(httpRequestBase);
            final var huaweiDataResult = this.mapperResult(contentBytes, HuaweiDataResult.class);
            final var zones = huaweiDataResult.getZones();
            if (CollectionUtil.isEmpty(zones)) {
                throw new RuntimeException("Failed to get zone id");
            } else {
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

    public HuaweiDataResult findDnsRecord(String domain, DNSRecordType recordType) {
        final var httpRequestBase = this.getRequestBuilder()
                .setUrl(this.getZoneUrl(domain))
                .addQueryStringParam("type", recordType.name())
                .setMethod(MethodType.GET)
                .build();
        byte[] contentBytes = this.executeRequest(httpRequestBase);
        return this.mapperResult(contentBytes, HuaweiDataResult.class);
    }

    public HuaweiDataResult addDnsRecord(String domain, String ip, DNSRecordType recordType) {
        final var jsonObject = new JsonObject().put("name", domain + ".")
                                               .put("type", recordType.name())
                                               .put("records", Collections.singletonList(ip));
        final var httpRequestBase = this.getRequestBuilder()
                              .setUrl(this.getZoneUrl(domain))
                              .setMethod(MethodType.POST)
                              .setBody(jsonObject.encode())
                              .build();
        final var contentBytes = this.executeRequest(httpRequestBase);
        return this.mapperResult(contentBytes, HuaweiDataResult.class);
    }

    public HuaweiDataResult updateDnsRecord(String doamin, String ip, DNSRecordType recordType) {
        return null;
    }

    private byte[] executeRequest(HttpRequestBase httpRequestBase) {
        try (CloseableHttpResponse httpResponse = this.closeableHttpClient.execute(httpRequestBase)){
            return this.getContentBytes(httpResponse);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return null;
        }
    }

    private byte[] getContentBytes(CloseableHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusLine()
                .getStatusCode() == 200) {
            return httpResponse.getEntity()
                    .getContent()
                    .readAllBytes();
        }
        return new byte[0];
    }

    private String getZoneUrl(String domain) {
        return this.getZoneUrl(domain, null);
    }

    private String getZoneUrl(String domain, String recordSetId) {
        String zoneId = this.zoneMap.get(domain);
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
