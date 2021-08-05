package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.rpc.DnspodSignatureComposer;
import com.zf1976.ddns.api.signer.rpc.RpcAPISignatureComposer;
import com.zf1976.ddns.pojo.DnspodDataResult;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Vertx;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 腾讯云DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:23 上午
 */
@SuppressWarnings({"FieldCanBeLocal", "DuplicatedCode"})
public class DnspodDnsAPI extends AbstractDnsAPI<DnspodDataResult> {

    private final String api = "https://dnspod.tencentcloudapi.com/";
    private final RpcAPISignatureComposer composer = DnspodSignatureComposer.getComposer();

    public DnspodDnsAPI(String id, String secret, Vertx vertx) {
        this(new BasicCredentials(id, secret), vertx);
    }

    public DnspodDnsAPI(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
        super(dnsApiCredentials, vertx);
    }

    /**
     * 查询主域名的解析记录，以记录类型区别ipv4 ipv6
     *
     * @param domain        域名/区分主域名跟多级域名
     * @param dnsRecordType 记录类型
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult findDnsRecords(String domain, DNSRecordType dnsRecordType) {
        this.checkDomain(domain);
        final var queryParam = this.getQueryParam("DescribeRecordList");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("RecordType", dnsRecordType.name());
        queryParam.put("Domain", extractDomain[0]);
        queryParam.put("Subdomain", "".equals(extractDomain[1]) ? "@" : extractDomain[1]);
        final var url = composer.toSignatureUrl(this.dnsApiCredentials.getAccessKeySecret(), this.api, MethodType.GET, queryParam);
        final var httpRequest = this.requestBuild(url);
        return this.sendRequest(httpRequest);
    }

    /**
     * 新增记录
     *
     * @param domain        域名/区分主域名跟多级域名
     * @param ip            ip值
     * @param dnsRecordType 记录类型
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult addDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        this.checkIp(ip);
        this.checkDomain(domain);
        final var queryParam = this.getQueryParam("CreateRecord");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("Domain", extractDomain[0]);
        queryParam.put("SubDomain", "".equals(extractDomain[1]) ? "@" : extractDomain[1]);
        queryParam.put("RecordType", dnsRecordType.name());
        queryParam.put("RecordLine", "默认");
        queryParam.put("Value", ip);
        final var url = this.composer.toSignatureUrl(this.dnsApiCredentials.getAccessKeySecret(), this.api, MethodType.GET, queryParam);
        final var httpRequest = this.requestBuild(url);
        return this.sendRequest(httpRequest);
    }

    /**
     * 更新记录
     *
     * @param recordId      记录ID
     * @param domain        域名/区分主域名跟多级域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult updateDnsRecord(String recordId, String domain, String ip, DNSRecordType dnsRecordType) {
        this.checkIp(ip);
        this.checkDomain(domain);
        final var queryParam = this.getQueryParam("ModifyRecord");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("Domain", extractDomain[0]);
        queryParam.put("SubDomain", "".equals(extractDomain[1]) ? "@" : extractDomain[1]);
        queryParam.put("RecordType", dnsRecordType.name());
        queryParam.put("RecordLine", "默认");
        queryParam.put("Value", ip);
        queryParam.put("RecordId", recordId);
        final var url = this.composer.toSignatureUrl(this.dnsApiCredentials.getAccessKeySecret(), this.api, MethodType.GET, queryParam);
        final var httpRequest = this.requestBuild(url);
        return this.sendRequest(httpRequest);
    }

    /**
     * 根据主域名、记录ID删除记录
     *
     * @param recordId 记录id
     * @param domain   域名/不区分顶级域名、多级域名
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult deleteDnsRecord(String recordId, String domain) {
        final var extractDomain = HttpUtil.extractDomain(domain);
        final var queryParam = this.getQueryParam("DeleteRecord");
        queryParam.put("Domain", extractDomain[0]);
        queryParam.put("RecordId", recordId);
        final var url = this.composer.toSignatureUrl(this.dnsApiCredentials.getAccessKeySecret(), this.api, MethodType.GET, queryParam);
        final var httpRequest = this.requestBuild(url);
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
        return DNSServiceType.DNSPOD.check(dnsServiceType);
    }

    private HttpRequest requestBuild(String url) {
        return HttpRequest.newBuilder()
                          .uri(URI.create(url))
                          .GET()
                          .build();
    }

    private DnspodDataResult sendRequest(HttpRequest request) {
        try {
            final var body = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                            .body();
            return this.mapperResult(body, DnspodDataResult.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> getQueryParam(String action) {
        Map<String, Object> params = new HashMap<>();
        params.put("Nonce", new Random().nextInt(java.lang.Integer.MAX_VALUE) + System.currentTimeMillis());
        params.put("Timestamp", System.currentTimeMillis() / 1000);
        params.put("SecretId", this.dnsApiCredentials.getAccessKeyId());
        params.put("Action", action);
        params.put("Version", "2021-03-23");
        params.put("SignatureMethod", "HmacSHA256");
        return params;
    }
}
