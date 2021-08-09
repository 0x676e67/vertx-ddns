package com.zf1976.ddns.api.provider;

import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.enums.DnsRecordType;
import com.zf1976.ddns.api.enums.HttpMethod;
import com.zf1976.ddns.util.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author mac
 * @date 2021/7/18
 */
@SuppressWarnings({"RedundantCast"})
public abstract class AbstractDnsProvider<T, A> implements DnsRecordProvider<T> {

    protected final Logger log = LogManager.getLogger("[AbstractDnsApi]");
    public static final int DEFAULT_CONNECT_TIMEOUT = 100000;
    protected final DnsApiCredentials dnsApiCredentials;
    protected final HttpClient httpClient = HttpClient.newBuilder()
                                                      .connectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT))
                                                      .executor(Executors.newSingleThreadExecutor())
                                                      .build();
    protected final WebClient webClient;

    protected AbstractDnsProvider(DnsApiCredentials dnsApiCredentials, Vertx vertx) {
        if (vertx == null) {
            throw new RuntimeException("Vert.x instance cannot be null");
        }
        Assert.notNull(dnsApiCredentials, "Credentials cannot been null!");
        this.dnsApiCredentials = dnsApiCredentials;
        final var webClientOptions = new WebClientOptions()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSsl(true);
        this.webClient = WebClient.create(vertx, webClientOptions);
    }

    protected void checkIp(String ip) {
        if (!HttpUtil.isIp(ip)) {
            throw new RuntimeException("ip：" + ip + " unqualified");
        }
    }

    protected void checkDomain(String domain) {
        if (HttpUtil.isDomain(domain)) {
            throw new RuntimeException("domain：" + domain + " unqualified");
        }
    }

    protected <E> E mapperResult(byte[] bytes, Class<E> tClass) {
        try {
            if (ObjectUtil.isEmpty(bytes)) {
                return null;
            }
            return Json.decodeValue(Buffer.buffer(bytes), tClass);
        } catch (DecodeException e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
            return null;
        }
    }

    protected <E> E mapperResult(String content, Class<E> tClass) {
        if (StringUtil.isEmpty(content)) {
            return null;
        }
        return this.mapperResult(content.getBytes(StandardCharsets.UTF_8), tClass);
    }

    protected T resultHandler(String body) {
        throw new UnsupportedOperationException();
    }

    protected T resultHandler(String body, A a) {
        throw new UnsupportedOperationException();
    }

    protected Future<T> resultHandlerAsync(HttpResponse<Buffer> responseFuture) {
        throw new UnsupportedOperationException();
    }

    protected Future<T> resultHandlerAsync(HttpResponse<Buffer> responseFuture, A a) {
        throw new UnsupportedOperationException();
    }

    protected Future<HttpResponse<Buffer>> sendRequestAsync(HttpRequest<Buffer> httpRequest) {
        throw new UnsupportedOperationException();
    }

    protected Future<HttpResponse<Buffer>> sendRequestAsync(HttpRequest<Buffer> httpRequest, JsonObject data) {
        throw new UnsupportedOperationException();
    }


    protected Future<HttpResponse<Buffer>> sendRequestAsync(String url, HttpMethod methodType) {
        return this.sendRequestAsync(url, (JsonObject) null, methodType);
    }

    protected Future<HttpResponse<Buffer>> sendRequestAsync(String url, JsonObject data, HttpMethod methodType) {
        throw new UnsupportedOperationException();
    }

    protected String concatUrl(String first, String... more) {
        final var urlBuilder = new StringBuilder(first);
        for (String path : more) {
            urlBuilder.append("/")
                      .append(path);
        }
        return urlBuilder.toString();
    }

    protected Map<String, Object> getQueryParam(String recordId, String domain, A action) {
        return this.getQueryParam(recordId, domain, (String) null, (DnsRecordType) null, action);
    }

    protected Map<String, Object> getQueryParam(String domain, DnsRecordType dnsRecordType, A action) {
        return this.getQueryParam((String) null, domain, (String) null, dnsRecordType, action);
    }

    protected Map<String, Object> getQueryParam(String domain, String ip, DnsRecordType dnsRecordType, A action) {
        return this.getQueryParam((String) null, domain, ip, dnsRecordType, action);
    }

    protected Map<String, Object> getQueryParam(String recordId,
                                                String domain,
                                                String ip,
                                                DnsRecordType dnsRecordType,
                                                A action) {

        throw new UnsupportedOperationException();
    }
}
