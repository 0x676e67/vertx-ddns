package com.zf1976.ddns.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signature.rpc.AliyunSignatureComposer;
import com.zf1976.ddns.api.signature.rpc.RpcAPISignatureComposer;
import com.zf1976.ddns.pojo.AliyunDataResult;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.ParameterHelper;
import com.zf1976.ddns.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云DNS
 *
 * @author mac
 * @date 2021/7/14
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue", "DuplicatedCode"})
public class AliyunDnsAPI extends AbstractDnsAPI {

    private final Logger log = LogManager.getLogger("[AliyunDnsAPI]");
    private final String api = "https://alidns.aliyuncs.com";
    private final ObjectMapper objectMapper;
    private final RpcAPISignatureComposer rpcSignatureComposer = AliyunSignatureComposer.getComposer();

    public AliyunDnsAPI(String accessKeyId, String accessKeySecret) {
        this(new BasicCredentials(accessKeyId, accessKeySecret));
    }

    public AliyunDnsAPI(DnsApiCredentials credentials) {
        super(credentials);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        this.objectMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 查询记录
     *
     * @param domain        域名/区分主域名、多级域名
     * @param dnsRecordType 记录类型
     * @return {@link AliyunDataResult}
     */
    public AliyunDataResult findDnsRecords(String domain, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam("DescribeDomainRecords");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("PageSize", "500");
        queryParam.put("TypeKeyWord", dnsRecordType.name());
        queryParam.put("DomainName", extractDomain[0]);
        if (!StringUtil.isEmpty(extractDomain[1])) {
            queryParam.put("RRKeyWord", extractDomain[1]);
        }
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 新增记录
     *
     * @param domain        域名/区分主域名、多级域名
     * @param ip            ip值
     * @param dnsRecordType 记录类型
     * @return {@link AliyunDataResult}
     */
    public AliyunDataResult addDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam("AddDomainRecord");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("Type", dnsRecordType.name());
        queryParam.put("Value", ip);
        queryParam.put("DomainName", extractDomain[0]);
        queryParam.put("RR", "".equals(extractDomain[1]) ? "@" : extractDomain[1]);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 更新记录
     *
     * @param recordId      记录id
     * @param domain        域名/区分主域名、多级域名
     * @param ip            ip值
     * @param dnsRecordType 记录类型
     */
    public AliyunDataResult updateDnsRecord(String recordId, String domain, String ip, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam("UpdateDomainRecord");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("RecordId", recordId);
        queryParam.put("Type", dnsRecordType.name());
        queryParam.put("Value", ip);
        queryParam.put("DomainName", extractDomain[0]);
        queryParam.put("RR", extractDomain[1]);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 删除记录
     *
     * @param recordId 记录id
     * @return {@link AliyunDataResult}
     */
    public AliyunDataResult deleteDnsRecord(String recordId) {
        final var queryParam = this.getQueryParam("DeleteDomainRecord");
        queryParam.put("RecordId", recordId);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    private HttpRequest requestBuild(MethodType methodType, Map<String, Object> queryParam) {
        final var url = this.rpcSignatureComposer.toSignatureUrl(this.dnsApiCredentials.getAccessKeySecret() + "&", this.api, methodType, queryParam);
        return HttpRequest.newBuilder()
                          .GET()
                          .uri(URI.create(url))
                          .build();
    }

    private AliyunDataResult sendRequest(HttpRequest request) {
        try {
            final var body = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                            .body();
            return this.objectMapper.readValue(body, AliyunDataResult.class);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getCause());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> getQueryParam(String action) {
        final var queryParam = new HashMap<String, Object>();
        queryParam.put("Format", "JSON");
        queryParam.put("AccessKeyId", this.dnsApiCredentials.getAccessKeyId());
        queryParam.put("Action", action);
        queryParam.put("SignatureMethod", rpcSignatureComposer.signatureMethod());
        queryParam.put("SignatureNonce", ParameterHelper.getUniqueNonce());
        queryParam.put("SignatureVersion", rpcSignatureComposer.getSignerVersion());
        queryParam.put("Version", "2015-01-09");
        queryParam.put("UserClientIp", HttpUtil.getCurrentHostIp());
        queryParam.put("Timestamp", ParameterHelper.getISO8601Time1(new Date()));
        return queryParam;
    }

}
