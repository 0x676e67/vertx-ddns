package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.HuaweiClient;
import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.pojo.HuaweiDataResult;
import com.zf1976.ddns.util.CollectionUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
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
    private final CloseableHttpClient closeableHttpClient = HttpClients.custom()
                                                                       .build();
    private final Map<String, String> zoneMap;

    public HuaweiDnsAPI(String id, String secret) {
        this(new BasicCredentials(id, secret));
    }

    public HuaweiDnsAPI(DnsApiCredentials dnsApiCredentials) {
        super(dnsApiCredentials);
        this.zoneMap = new HashMap<>();
        final var huaweiRequest = new HuaweiRequest(dnsApiCredentials);
        huaweiRequest.setUrl(api)
                     .setMethod(MethodType.GET);
        CloseableHttpResponse httpResponse = null;
        try {
            final var httpRequestBase = HuaweiClient.sign(huaweiRequest);
            httpResponse = this.closeableHttpClient.execute(httpRequestBase);
            if (httpResponse.getStatusLine()
                            .getStatusCode() == 200) {
                if (httpResponse.getEntity() != null) {
                    final var contentBytes = this.getContentBytes(httpResponse);
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
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e.getCause());
                }
            }
        }
    }

    public HuaweiDataResult findDnsRecord(String domain, DNSRecordType recordType) {
        final var huaweiRequest = this.getRequest()
                                      .setUrl(api + "/" + this.zoneMap.get(domain) + "/recordsets")
                                      .addQueryStringParam("type", recordType.name())
                                      .setMethod(MethodType.GET);
        CloseableHttpResponse httpResponse = null;
        try {
            final var httpRequestBase = HuaweiClient.sign(huaweiRequest);
            httpResponse = this.closeableHttpClient.execute(httpRequestBase);
            final var contentBytes = this.getContentBytes(httpResponse);
            return this.mapperResult(contentBytes, HuaweiDataResult.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return null;
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e.getCause());
                }
            }
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

    private HuaweiRequest getRequest() {
        return new HuaweiRequest(this.dnsApiCredentials);
    }
}
