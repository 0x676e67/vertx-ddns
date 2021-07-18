package com.zf1976.ddns.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.signature.aliyun.MethodType;
import com.zf1976.ddns.api.signature.aliyun.sign.RpcSignatureComposer;
import com.zf1976.ddns.pojo.AliyunDataResult;
import com.zf1976.ddns.util.Assert;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.ParameterHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** 阿里云DNS
 *
 * @author mac
 * @date 2021/7/14
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue", "DuplicatedCode"})
public class AliyunDnsApi extends AbstractDnsApi{

    private final Logger log = LogManager.getLogger("[AliyunDnsApi]");
    private final String api = "https://alidns.aliyuncs.com";
    private final DnsApiCredentials credentials;
    private final ObjectMapper objectMapper;
    private final RpcSignatureComposer rpcSignatureComposer;

    public AliyunDnsApi(String accessKeyId, String accessKeySecret) {
        this(new BasicCredentials(accessKeyId, accessKeySecret));
    }

    public AliyunDnsApi(DnsApiCredentials credentials) {
        Assert.notNull(credentials,"AlibabaCloudCredentials cannot been null!");
        this.credentials = credentials;
        this.rpcSignatureComposer = (RpcSignatureComposer) RpcSignatureComposer.getComposer();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        this.objectMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public AliyunDataResult findDnsRecords(String domain, String type) {
        final var queryParam = this.getQueryParam("DescribeDomainRecords");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("PageSize", "500");
        queryParam.put("TypeKeyWord", type);
        queryParam.put("DomainName", extractDomain[0]);
        queryParam.put("RRKeyWord", extractDomain[1]);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    public AliyunDataResult addDnsRecord(String domain, String ip, String type) {
        final var queryParam = this.getQueryParam("AddDomainRecord");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("Type", type);
        queryParam.put("Value", ip);
        queryParam.put("DomainName", extractDomain[0]);
        queryParam.put("RR", "".equals(extractDomain[1])? "@" : extractDomain[1]);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    public AliyunDataResult updateDnsRecord(String recordId, String domain, String ip, String type) {
        final var queryParam = this.getQueryParam("UpdateDomainRecord");
        final var extractDomain = HttpUtil.extractDomain(domain);
        queryParam.put("RecordId", recordId);
        queryParam.put("Type", type);
        queryParam.put("Value", ip);
        queryParam.put("DomainName", extractDomain[0]);
        queryParam.put("RR", extractDomain[1]);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    public AliyunDataResult removeDnsRecord(String recordId) {
        final var queryParam = this.getQueryParam("DeleteDomainRecord");
        queryParam.put("RecordId", recordId);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    private HttpRequest requestBuild(MethodType methodType, Map<String, String> queryParam) {
        final var url = this.rpcSignatureComposer.toUrl(this.credentials.getAccessKeySecret(), this.api, methodType, queryParam);
        return HttpRequest.newBuilder()
                          .GET()
                          .uri(URI.create(url))
                          .build();
    }

    private AliyunDataResult sendRequest(HttpRequest request) {
        try {
            final var body = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return this.objectMapper.readValue(body, AliyunDataResult.class);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getCause());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, String> getQueryParam(String action) {
        final var queryParam = new HashMap<String, String>();
        queryParam.put("Format", "JSON");
        queryParam.put("AccessKeyId", this.credentials.getAccessKeyId());
        queryParam.put("Action", action);
        queryParam.put("SignatureMethod","HMAC-SHA1");
        queryParam.put("SignatureNonce", ParameterHelper.getUniqueNonce());
        queryParam.put("SignatureVersion","1.0");
        queryParam.put("Version", "2015-01-09");
        queryParam.put("Timestamp",ParameterHelper.getISO8601Time(new Date()));
        return queryParam;
    }

}
