package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DNSRecordType;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.rpc.AliyunSignatureComposer;
import com.zf1976.ddns.api.signer.rpc.RpcAPISignatureComposer;
import com.zf1976.ddns.pojo.AliyunDataResult;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.ParameterHelper;
import com.zf1976.ddns.util.StringUtil;
import com.zf1976.ddns.verticle.DNSServiceType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 阿里云DNS
 *
 * @author mac
 * @date 2021/7/14
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue", "DuplicatedCode"})
public class AliyunDnsApi extends AbstractDnsApi<AliyunDataResult, AliyunDnsApi.Action> {

    private final Logger log = LogManager.getLogger("[AliyunDnsApi]");
    private final String api = "https://alidns.aliyuncs.com/";
    private final RpcAPISignatureComposer rpcSignatureComposer = AliyunSignatureComposer.getComposer();

    public AliyunDnsApi(String accessKeyId, String accessKeySecret, Vertx vertx) {
        this(new BasicCredentials(accessKeyId, accessKeySecret), vertx);
    }

    public AliyunDnsApi(DnsApiCredentials credentials, Vertx vertx) {
        super(credentials, vertx);
    }

    /**
     * 查询记录
     *
     * @param domain        域名/区分主域名、多级域名
     * @param dnsRecordType 记录类型
     * @return {@link AliyunDataResult}
     */
    @Override
    public AliyunDataResult findDnsRecords(String domain, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(domain, dnsRecordType, Action.DESCRIBE);
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
        final var queryParam = this.getQueryParam(domain, ip, dnsRecordType, Action.ADD);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 更新记录
     *
     * @param id            记录id
     * @param domain        域名/区分主域名、多级域名
     * @param ip            ip值
     * @param dnsRecordType 记录类型
     */
    public AliyunDataResult updateDnsRecord(String id, String domain, String ip, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(id, domain, ip, dnsRecordType, Action.UPDATE);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }


    /**
     * 删除域名记录
     *
     * @param id     记录id（记录唯一凭证）
     * @param domain 阿里云域名可以忽略
     * @return {@link AliyunDataResult}
     */
    @Override
    public AliyunDataResult deleteDnsRecord(String id, String domain) {
        final var queryParam = this.getQueryParam(id, domain, Action.DELETE);
        final var httpRequest = this.requestBuild(MethodType.GET, queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 异步版本
     *
     * @param domain        域名
     * @param dnsRecordType 记录类型
     * @return {@link Future<AliyunDataResult>}
     */
    @Override
    public Future<AliyunDataResult> asyncFindDnsRecords(String domain, DNSRecordType dnsRecordType) {
        final var initQueryParam = this.getQueryParam(domain, dnsRecordType, Action.DESCRIBE);
        final var requestUrl = this.getRequestUrl(MethodType.GET, initQueryParam);
        return this.webClient.getAbs(requestUrl)
                             .send()
                             .compose(this::resultFuture);
    }

    /**
     * 异步版本
     *
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link Future<AliyunDataResult>}
     */
    @Override
    public Future<AliyunDataResult> asyncAddDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        final var initQueryParam = this.getQueryParam(domain, ip, dnsRecordType, Action.ADD);
        final var requestUrl = this.getRequestUrl(MethodType.GET, initQueryParam);
        return this.webClient.getAbs(requestUrl)
                             .send()
                             .compose(this::resultFuture);
    }

    /**
     * 异步版本
     *
     * @param id            id
     * @param domain        域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link Future<AliyunDataResult>}
     */
    @Override
    public Future<AliyunDataResult> asyncUpdateDnsRecord(String id,
                                                         String domain,
                                                         String ip,
                                                         DNSRecordType dnsRecordType) {
        final var initQueryParam = this.getQueryParam(id, domain, ip, dnsRecordType, Action.UPDATE);
        final var requestUrl = this.getRequestUrl(MethodType.GET, initQueryParam);
        return this.webClient.getAbs(requestUrl)
                             .send()
                             .compose(this::resultFuture);
    }

    /**
     * 异步版本
     *
     * @param id     id
     * @param domain 域名
     * @return {@link Future<AliyunDataResult>}
     */
    @Override
    public Future<AliyunDataResult> asyncDeleteDnsRecord(String id, String domain) {
        final var initQueryParam = this.getQueryParam(id, domain, Action.DELETE);
        final var requestUrl = this.getRequestUrl(MethodType.GET, initQueryParam);
        return this.webClient.getAbs(requestUrl)
                             .send()
                             .compose(this::resultFuture);
    }

    private HttpRequest requestBuild(MethodType methodType, Map<String, Object> queryParam) {
        final var requestUrl = this.getRequestUrl(methodType, queryParam);
        return HttpRequest.newBuilder()
                          .GET()
                          .uri(URI.create(requestUrl))
                          .build();
    }

    /**
     * 是否支持
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    @Override
    public boolean supports(DNSServiceType dnsServiceType) {
        return DNSServiceType.ALIYUN.check(dnsServiceType);
    }

    private Map<String, String> urlToQueryParam(String url) {
        final var canonicalizeString = url.substring(url.lastIndexOf("?") + 1);
        final var split = canonicalizeString.split("&");
        Map<String, String> queryParam = new LinkedHashMap<>(split.length + 2);
        for (String kv : split) {
            final var kvArrays = kv.split("=");
            queryParam.put(kvArrays[0], kvArrays[1]);
        }
        return queryParam;
    }

    private String getRequestUrl(MethodType methodType, Map<String, Object> queryParam) {
        return this.rpcSignatureComposer.toSignatureUrl(this.dnsApiCredentials.getAccessKeySecret() + "&", this.api, methodType, queryParam);
    }

    private Future<AliyunDataResult> resultFuture(io.vertx.ext.web.client.HttpResponse<Buffer> responseFuture) {
        final var body = responseFuture.bodyAsString();
        final AliyunDataResult aliyunDataResult;
        try {
            aliyunDataResult = this.mapperResult(body, AliyunDataResult.class);
            return Future.succeededFuture(aliyunDataResult);
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    private AliyunDataResult sendRequest(HttpRequest request) {
        try {
            final var body = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                            .body();
            return this.mapperResult(body, AliyunDataResult.class);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getCause());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> getCommonQueryParam(Action action) {
        final var queryParam = new HashMap<String, Object>();
        queryParam.put("Format", "JSON");
        queryParam.put("AccessKeyId", this.dnsApiCredentials.getAccessKeyId());
        queryParam.put("Action", action.value);
        queryParam.put("SignatureMethod", rpcSignatureComposer.signatureMethod());
        queryParam.put("SignatureNonce", ParameterHelper.getUniqueNonce());
        queryParam.put("SignatureVersion", rpcSignatureComposer.getSignerVersion());
        queryParam.put("Version", "2015-01-09");
        queryParam.put("Timestamp", ParameterHelper.getISO8601Time1(new Date()));
        return queryParam;
    }

    protected Map<String, Object> getQueryParam(String recordId,
                                                String domain,
                                                String ip,
                                                DNSRecordType dnsRecordType,
                                                AliyunDnsApi.Action action) {
        final var queryParam = this.getCommonQueryParam(action);
        final var extractDomain = HttpUtil.extractDomain(domain);
        switch (action) {
            case ADD -> {
                queryParam.put("Type", dnsRecordType.name());
                queryParam.put("Value", ip);
                queryParam.put("DomainName", extractDomain[0]);
                queryParam.put("RR", "".equals(extractDomain[1]) ? "@" : extractDomain[1]);
            }
            case DELETE -> queryParam.put("RecordId", recordId);
            case UPDATE -> {
                queryParam.put("RecordId", recordId);
                queryParam.put("Type", dnsRecordType.name());
                queryParam.put("Value", ip);
                queryParam.put("DomainName", extractDomain[0]);
                queryParam.put("RR", extractDomain[1]);
            }
            case DESCRIBE -> {
                queryParam.put("PageSize", "500");
                queryParam.put("TypeKeyWord", dnsRecordType.name());
                queryParam.put("DomainName", extractDomain[0]);
                if (!StringUtil.isEmpty(extractDomain[1])) {
                    queryParam.put("RRKeyWord", extractDomain[1]);
                }
            }
        }
        return queryParam;
    }

    protected enum Action {

        DESCRIBE("DescribeDomainRecords"),
        ADD("AddDomainRecord"),
        UPDATE("UpdateDomainRecord"),
        DELETE("DeleteDomainRecord");

        public final String value;

        Action(String action) {
            value = action;
        }
    }

}
