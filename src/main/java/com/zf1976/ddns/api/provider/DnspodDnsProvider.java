package com.zf1976.ddns.api.provider;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.auth.DnsProviderCredentials;
import com.zf1976.ddns.api.provider.exception.DnsServiceResponseException;
import com.zf1976.ddns.api.signer.rpc.DnspodSignatureComposer;
import com.zf1976.ddns.api.signer.rpc.RpcAPISignatureComposer;
import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.enums.DnsRecordType;
import com.zf1976.ddns.enums.HttpMethod;
import com.zf1976.ddns.pojo.DnspodDataResult;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.LogUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 腾讯云DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:23 上午
 */
public class DnspodDnsProvider extends AbstractDnsProvider<DnspodDataResult, DnspodDnsProvider.Action> {

    private final Logger log = LogManager.getLogger("[DnspodDnsProvider]");
    private final RpcAPISignatureComposer composer = DnspodSignatureComposer.getComposer();

    public DnspodDnsProvider(String id, String secret, Vertx vertx) {
        this(new BasicCredentials(id, secret), vertx);
    }

    public DnspodDnsProvider(DnsProviderCredentials dnsApiCredentials, Vertx vertx) {
        super(dnsApiCredentials, vertx);
    }

    /**
     * 查询主域名的解析记录，以记录类型区别ipv4 ipv6
     *
     * @param domain        域名/区分主域名跟多级域名
     * @param dnsRecordType 记录类型
     * @return {@link DnspodDataResult}
     */
    public DnspodDataResult findDnsRecordList(String domain, DnsRecordType dnsRecordType) {
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
    public DnspodDataResult createDnsRecord(String domain, String ip, DnsRecordType dnsRecordType) {
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
    public DnspodDataResult modifyDnsRecord(String id, String domain, String ip, DnsRecordType dnsRecordType) {
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
    public Future<DnspodDataResult> findDnsRecordListAsync(String domain, DnsRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(domain, dnsRecordType, Action.DESCRIBE);
        final var url = this.requestUrlBuild(queryParam);
        return this.sendRequestAsync(url)
                   .compose(this::bodyHandlerAsync);
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
    public Future<DnspodDataResult> createDnsRecordAsync(String domain, String ip, DnsRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(domain, ip, dnsRecordType, Action.CREATE);
        final var url = this.requestUrlBuild(queryParam);
        return this.sendRequestAsync(url)
                   .compose(this::bodyHandlerAsync);
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
    public Future<DnspodDataResult> modifyDnsRecordAsync(String id,
                                                         String domain,
                                                         String ip,
                                                         DnsRecordType dnsRecordType) {
        final var queryParam = this.getQueryParam(id, domain, ip, dnsRecordType, Action.MODIFY);
        final var url = this.requestUrlBuild(queryParam);
        return this.sendRequestAsync(url)
                   .compose(this::bodyHandlerAsync);
    }

    /**
     * 异步版本
     *
     * @param id     id
     * @param domain 域名
     * @return {@link Future<DnspodDataResult>}
     */
    @Override
    public Future<DnspodDataResult> deleteDnsRecordAsync(String id, String domain) {
        final var queryParam = this.getQueryParam(id, domain, Action.DELETE);
        final var url = this.requestUrlBuild(queryParam);
        return this.sendRequestAsync(url)
                   .compose(this::bodyHandlerAsync);
    }

    /**
     * 是否支持
     *
     * @param dnsServiceType DNS服务商类型
     * @return {@link boolean}
     */
    @Override
    public boolean support(DnsProviderType dnsServiceType) {
        return DnsProviderType.DNSPOD.check(dnsServiceType);
    }

    @Override
    public Future<Boolean> supportAsync(DnsProviderType dnsServiceType) {
        if (this.support(dnsServiceType)) {
            return Future.succeededFuture(true);
        }
        return Future.failedFuture("The :" + dnsServiceType.name() + " DNS service provider is not supported");
    }

    @Override
    protected Future<io.vertx.ext.web.client.HttpResponse<Buffer>> sendRequestAsync(String url) {
        return this.webClient.getAbs(url).send();
    }

    @Override
    protected DnspodDataResult bodyHandler(String body) {
        final var dnspodDataResult = this.mapperResult(body, DnspodDataResult.class);
        if (dnspodDataResult != null && dnspodDataResult.getResponse() != null) {
            final var error = dnspodDataResult.getResponse()
                                              .getError();
            if (error != null) {
                throw new DnsServiceResponseException(error.getMessage());
            }
        }
        return dnspodDataResult;
    }

    @Override
    protected Future<DnspodDataResult> bodyHandlerAsync(io.vertx.ext.web.client.HttpResponse<Buffer> httpResponse) {
        try {
            final var body = httpResponse.bodyAsString();
            final DnspodDataResult aliyunDataResult = this.bodyHandler(body);
            return Future.succeededFuture(aliyunDataResult);
        } catch (Exception e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
            return Future.failedFuture(e);
        }
    }

    private String requestUrlBuild(Map<String, Object> queryParam) {
        final String api = "https://dnspod.tencentcloudapi.com/";
        return this.composer.toSignatureUrl(this.dnsProviderCredentials.getAccessKeySecret(), api, HttpMethod.GET, queryParam);
    }

    private HttpRequestBase requestBuild(Map<String, Object> queryParam) {
        final var requestUrl = this.requestUrlBuild(queryParam);
        return new HttpGet(requestUrl);
    }

    private Map<String, Object> getCommonQueryParam(Action action) {
        Map<String, Object> params = new HashMap<>();
        params.put("Nonce", new Random().nextInt(java.lang.Integer.MAX_VALUE) + System.currentTimeMillis());
        params.put("Timestamp", System.currentTimeMillis() / 1000);
        params.put("SecretId", this.dnsProviderCredentials.getAccessKeyId());
        params.put("Action", action.value);
        params.put("Version", "2021-03-23");
        params.put("SignatureMethod", "HmacSHA256");
        return params;
    }

    protected Map<String, Object> getQueryParam(String recordId,
                                                String domain,
                                                String ip,
                                                DnsRecordType dnsRecordType,
                                                DnspodDnsProvider.Action action) {
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
                                DnsRecordType dnsRecordType,
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
