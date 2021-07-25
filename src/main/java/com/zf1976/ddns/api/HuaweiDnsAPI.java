package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.pojo.HuaweiDataResult;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.StringUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 华为DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:25 上午
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
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

    private byte[] executeRequest(HttpRequestBase httpRequestBase) {
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = this.closeableHttpClient.execute(httpRequestBase);
            return this.getContentBytes(httpResponse);
        } catch (IOException e) {
            log.error(e.getMessage(), e.getCause());
            return null;
        } finally {
            this.closeHttpResponse(httpResponse);
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
        String zoneId = this.zoneMap.get(domain);
        if (StringUtil.isEmpty(zoneId)) {
            throw new RuntimeException("Resolved primary domain name:" + domain + "does not exist");
        }
        return this.api + "/" + zoneId + "/recordsets";
    }

    private void closeHttpResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e.getCause());
            }
        }
    }

    private HuaweiRequest getRequestBuilder() {
        return HuaweiRequest.newBuilder(this.dnsApiCredentials);
    }
}
