package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signature.rpc.DnspodSignatureComposer;
import com.zf1976.ddns.api.signature.rpc.RpcAPISignatureComposer;
import com.zf1976.ddns.pojo.DnspodDataResult;
import com.zf1976.ddns.util.HttpUtil;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯云DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:23 上午
 */
@SuppressWarnings({"FieldCanBeLocal", "DuplicatedCode"})
public class DnspodDnsAPI extends AbstractDnsAPI {

    private final Logger log = LogManager.getLogger("[DnspodDnsAPI]");
    private final String api = "https://dnspod.tencentcloudapi.com";
    private final RpcAPISignatureComposer rpcAPISignatureComposer = DnspodSignatureComposer.getComposer();

    public DnspodDnsAPI(String id, String secret) {
        this(new BasicCredentials(id, secret));
    }

    public DnspodDnsAPI(DnsApiCredentials dnsApiCredentials) {
        super(dnsApiCredentials);
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
        final var httpRequest = this.requestBuild(queryParam);
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
        final var httpRequest = this.requestBuild(queryParam);
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
        final var httpRequest = this.requestBuild(queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 根据主域名、记录ID删除记录
     *
     * @param recordId   记录id
     * @param mainDomain 主域名
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult deleteDnsRecord(String recordId, String mainDomain) {
        this.checkDomain(mainDomain);
        final var queryParam = this.getQueryParam("DeleteRecord");
        queryParam.put("Domain", mainDomain);
        queryParam.put("RecordId", recordId);
        final var httpRequest = this.requestBuild(queryParam);
        return this.sendRequest(httpRequest);
    }

    private HttpRequest requestBuild(Map<String, Object> queryParam) {
        final var url = this.rpcAPISignatureComposer.toUrl(this.dnsApiCredentials.getAccessKeySecret(), this.api, MethodType.GET, queryParam);
        return HttpRequest.newBuilder()
                          .header("Content-type", "application/x-www-form-urlencoded")
                          .uri(URI.create(url))
                          .GET()
                          .build();
    }

    private DnspodDataResult sendRequest(HttpRequest request) {
        try {
            final var body = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                            .body();
            return Json.decodeValue(body, DnspodDataResult.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> getQueryParam(String action) {
        Map<String, Object> params = new HashMap<>();
        params.put("Nonce", System.currentTimeMillis());
        params.put("Timestamp", System.currentTimeMillis() / 1000);
        params.put("SecretId", this.dnsApiCredentials.getAccessKeyId());
        params.put("Action", action);
        params.put("Version", "2021-03-23");
        params.put("SignatureMethod", this.rpcAPISignatureComposer.signatureMethod());
        return params;
    }
}
