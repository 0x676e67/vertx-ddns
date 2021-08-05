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
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

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
public class DnspodDnsApi extends AbstractDnsApi<DnspodDataResult, DnspodDnsApi.Action> {

    private final RpcAPISignatureComposer composer = DnspodSignatureComposer.getComposer();

    public DnspodDnsApi(String id, String secret, Vertx vertx) {
        this(new BasicCredentials(id, secret), vertx);
    }

    public DnspodDnsApi(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
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
        final var queryParam = this.getQueryParam(domain, dnsRecordType, Action.DESCRIBE);
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
    public DnspodDataResult createDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(domain, ip, dnsRecordType, Action.CREATE);
        final var httpRequest = this.requestBuild(queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 更新记录
     *
     * @param id            记录ID
     * @param domain        域名/区分主域名跟多级域名
     * @param ip            ip
     * @param dnsRecordType 记录类型
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult modifyDnsRecord(String id, String domain, String ip, DNSRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(id, domain, ip, dnsRecordType, Action.MODIFY);
        final var httpRequest = this.requestBuild(queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 根据主域名、记录ID删除记录
     *
     * @param id     记录id
     * @param domain 域名/不区分顶级域名、多级域名
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult deleteDnsRecord(String id, String domain) {
        final var queryParam = this.getQueryParam(id, domain, Action.DELETE);
        final var httpRequest = this.requestBuild(queryParam);
        return this.sendRequest(httpRequest);
    }

    /**
     * 异步版本
     *
     * @param domain        域名
     * @param dnsRecordType 记录类型
     * @return {@link Future<DnspodDataResult>}
     */
    @Override
    public Future<DnspodDataResult> asyncFindDnsRecords(String domain, DNSRecordType dnsRecordType) {
        final var initQueryParam = this.getQueryParam(domain, dnsRecordType, Action.DESCRIBE);
        final var requestUrl = this.requestUrlBuild(initQueryParam);
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
     * @return {@link Future<DnspodDataResult>}
     */
    @Override
    public Future<DnspodDataResult> asyncCreateDnsRecord(String domain, String ip, DNSRecordType dnsRecordType) {
        final var initQueryParam = this.getQueryParam(domain, ip, dnsRecordType, Action.CREATE);
        final var requestUrl = this.requestUrlBuild(initQueryParam);
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
     * @return {@link Future<DnspodDataResult>}
     */
    @Override
    public Future<DnspodDataResult> asyncModifyDnsRecord(String id,
                                                         String domain,
                                                         String ip,
                                                         DNSRecordType dnsRecordType) {
        final var initQueryParam = this.getQueryParam(id, domain, ip, dnsRecordType, Action.MODIFY);
        final var requestUrl = this.requestUrlBuild(initQueryParam);
        return this.webClient.getAbs(requestUrl)
                             .send()
                             .compose(this::resultFuture);
    }

    /**
     * 异步版本
     *
     * @param id     id
     * @param domain 域名
     * @return {@link Future<DnspodDataResult>}
     */
    @Override
    public Future<DnspodDataResult> asyncDeleteDnsRecord(String id, String domain) {
        final var initQueryParam = this.getQueryParam(id, domain, Action.DELETE);
        final var requestUrl = this.requestUrlBuild(initQueryParam);
        return this.webClient.getAbs(requestUrl)
                             .send()
                             .compose(this::resultFuture);
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

    private String requestUrlBuild(Map<String, Object> queryParam) {
        final String api = "https://dnspod.tencentcloudapi.com/";
        return this.composer.toSignatureUrl(this.dnsApiCredentials.getAccessKeySecret(), api, MethodType.GET, queryParam);
    }

    private HttpRequest requestBuild(Map<String, Object> queryParam) {
        final var requestUrl = this.requestUrlBuild(queryParam);
        return HttpRequest.newBuilder()
                          .GET()
                          .uri(URI.create(requestUrl))
                          .build();
    }

    private Future<DnspodDataResult> resultFuture(io.vertx.ext.web.client.HttpResponse<Buffer> responseFuture) {
        final var body = responseFuture.bodyAsString();
        final DnspodDataResult aliyunDataResult;
        try {
            aliyunDataResult = this.mapperResult(body, DnspodDataResult.class);
            return Future.succeededFuture(aliyunDataResult);
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    private DnspodDataResult sendRequest(HttpRequest httpRequest) {
        try {
            final var body = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
                                            .body();
            return this.mapperResult(body, DnspodDataResult.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> getCommonQueryParam(Action action) {
        Map<String, Object> params = new HashMap<>();
        params.put("Nonce", new Random().nextInt(java.lang.Integer.MAX_VALUE) + System.currentTimeMillis());
        params.put("Timestamp", System.currentTimeMillis() / 1000);
        params.put("SecretId", this.dnsApiCredentials.getAccessKeyId());
        params.put("Action", action.value);
        params.put("Version", "2021-03-23");
        params.put("SignatureMethod", "HmacSHA256");
        return params;
    }

    protected Map<String, Object> getQueryParam(String recordId,
                                                String domain,
                                                String ip,
                                                DNSRecordType dnsRecordType,
                                                DnspodDnsApi.Action action) {
        final var queryParam = this.getCommonQueryParam(action);
        final var extractDomain = HttpUtil.extractDomain(domain);
        switch (action) {
            case CREATE -> putCommonParam(ip, dnsRecordType, queryParam, extractDomain);
            case DELETE -> {
                queryParam.put("Domain", extractDomain[0]);
                queryParam.put("RecordId", recordId);
            }
            case MODIFY -> {
                putCommonParam(ip, dnsRecordType, queryParam, extractDomain);
                queryParam.put("RecordId", recordId);
            }
            case DESCRIBE -> {
                queryParam.put("RecordType", dnsRecordType.name());
                queryParam.put("Domain", extractDomain[0]);
                queryParam.put("Subdomain", "".equals(extractDomain[1]) ? "@" : extractDomain[1]);
            }
        }
        return queryParam;
    }

    private void putCommonParam(String ip,
                                DNSRecordType dnsRecordType,
                                Map<String, Object> queryParam,
                                String[] extractDomain) {
        queryParam.put("Domain", extractDomain[0]);
        queryParam.put("SubDomain", "".equals(extractDomain[1]) ? "@" : extractDomain[1]);
        queryParam.put("RecordType", dnsRecordType.name());
        queryParam.put("RecordLine", "默认");
        queryParam.put("Value", ip);
    }

    protected enum Action {
        DESCRIBE("DescribeRecordList"),
        CREATE("CreateRecord"),
        DELETE("DeleteRecord"),
        MODIFY("ModifyRecord");

        private final String value;

        Action(String value) {
            this.value = value;
        }
    }
}
