package com.zf1976.ddns.api.provider;

import com.zf1976.ddns.api.auth.ProviderCredentials;
import com.zf1976.ddns.api.provider.exception.DnsServiceResponseException;
import com.zf1976.ddns.enums.DnsRecordType;
import com.zf1976.ddns.enums.HttpMethod;
import com.zf1976.ddns.util.Assert;
import com.zf1976.ddns.util.LogUtil;
import com.zf1976.ddns.util.ObjectUtil;
import com.zf1976.ddns.util.StringUtil;
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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author mac
 * 2021/7/18
 */
@SuppressWarnings({"RedundantCast"})
public abstract class AbstractDnsProvider<T, A> implements DnsRecordProvider<T> {

    protected final Logger log = LogManager.getLogger("[AbstractDnsProvider]");
    public static final int DEFAULT_CONNECT_TIMEOUT = 60000;
    protected ProviderCredentials dnsProviderCredentials;
    protected final HttpClient httpClient = HttpClient.newBuilder()
                                                      .connectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT))
                                                      .executor(Executors.newSingleThreadExecutor())
                                                      .build();
    protected final CloseableHttpClient closeableHttpClient = HttpClients.custom()
                                                                         .setConnectionTimeToLive(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                                                                         .build();
    protected final WebClient webClient;

    protected AbstractDnsProvider(ProviderCredentials dnsApiCredentials, Vertx vertx) {
        if (vertx == null) {
            throw new RuntimeException("Vert.x instance cannot be null");
        }
        Assert.notNull(dnsApiCredentials, "Credentials cannot been null!");
        this.dnsProviderCredentials = dnsApiCredentials;
        final var webClientOptions = new WebClientOptions()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSsl(true);
        this.webClient = WebClient.create(vertx, webClientOptions);
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

    protected T bodyHandler(String body) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected T sendRequest(HttpRequestBase request) {
        try (final var httpResponse = this.closeableHttpClient.execute(request)) {
            final var body = this.getBody(httpResponse);
            return this.bodyHandler(body);
        } catch (IOException e) {
            LogUtil.printDebug(log, e.getMessage(), e.getCause());
            throw new DnsServiceResponseException(e.getMessage(), e.getCause());
        }
    }

    protected T bodyHandler(String body, A a) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected Future<T> bodyHandlerAsync(HttpResponse<Buffer> httpResponse) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected Future<T> bodyHandlerAsync(HttpResponse<Buffer> responseFuture, A a) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected Future<HttpResponse<Buffer>> sendRequestAsync(HttpRequest<Buffer> httpRequest) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected Future<HttpResponse<Buffer>> sendRequestAsync(HttpRequest<Buffer> httpRequest, JsonObject data) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected Future<HttpResponse<Buffer>> sendRequestAsync(String url) {
        throw new UnsupportedOperationException("unrealized");
    }


    protected Future<HttpResponse<Buffer>> sendRequestAsync(String url, HttpMethod methodType) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected Future<HttpResponse<Buffer>> sendRequestAsync(String url, JsonObject data, HttpMethod methodType) {
        throw new UnsupportedOperationException("unrealized");
    }

    protected String concatUrl(String first, String... more) {
        final var urlBuilder = new StringBuilder(first);
        for (String path : more) {
            urlBuilder.append("/")
                      .append(path);
        }
        return urlBuilder.toString();
    }

    protected String getBody(CloseableHttpResponse httpResponse) throws IOException {
        final var contentBytes = this.getBodyBytes(httpResponse);
        return new String(contentBytes, StandardCharsets.UTF_8);
    }

    protected byte[] getBodyBytes(CloseableHttpResponse httpResponse) throws IOException {
        final var content = httpResponse.getEntity()
                                        .getContent();
        final var statusCode = httpResponse.getStatusLine()
                                           .getStatusCode();
        if (statusCode == 200 || statusCode == 202 || statusCode == 204) {
            return content.readAllBytes();
        } else {
            LogUtil.printDebug(log, Json.decodeValue(Buffer.buffer(content.readAllBytes())));
        }
        return new byte[0];
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

    @Override
    public void reloadCredentials(ProviderCredentials dnsProviderCredentials) {
        this.dnsProviderCredentials = dnsProviderCredentials;
    }
}
