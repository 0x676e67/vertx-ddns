package com.zf1976.ddns.api.signer.service;

import com.zf1976.ddns.api.enums.HttpMethod;
import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.api.signer.HuaweiSigner;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author ant
 * Create by Ant on 2021/8/6 1:39 PM
 */
@SuppressWarnings("DuplicatedCode")
public class AsyncHuaweiAccessServiceImpl extends HuaweiAccessService<HttpRequest<Buffer>> {

    private final WebClient webClient;

    public AsyncHuaweiAccessServiceImpl(String ak, String sk, WebClient webClient) {
        super(ak, sk);
        this.webClient = webClient;
    }

    @SuppressWarnings("SameParameterValue")
    private HttpRequest<Buffer> createRequest(String url, HttpMethod httpMethod) {
        HttpRequest<Buffer> httpRequest;
        if (httpMethod == HttpMethod.POST) {
            httpRequest = this.webClient.postAbs(url);
        } else if (httpMethod == HttpMethod.PUT) {
            httpRequest = this.webClient.putAbs(url);
        } else if (httpMethod == HttpMethod.PATCH) {
            httpRequest = this.webClient.patchAbs(url);
        } else if (httpMethod == HttpMethod.GET) {
            httpRequest = this.webClient.getAbs(url);
        } else if (httpMethod == HttpMethod.DELETE) {
            httpRequest = this.webClient.deleteAbs(url);
        } else if (httpMethod == HttpMethod.OPTIONS) {
            httpRequest = this.webClient.requestAbs(io.vertx.core.http.HttpMethod.OPTIONS, url);
        } else {
            if (httpMethod != HttpMethod.HEAD) {
                throw new RuntimeException("Unknown HTTP method name: " + httpMethod);
            }

            httpRequest = this.webClient.headAbs(url);
        }
        return httpRequest;
    }

    public HttpRequest<Buffer> access(String url,
                                      Map<String, String> headers,
                                      String content,
                                      HttpMethod httpMethod) throws Exception {
        HuaweiRequest request = this.newRequest(url, httpMethod);


        for (String k : headers.keySet()) {
            request.addHeader(k, headers.get(k));
        }

        request.setBody(content);
        HuaweiSigner signer = new HuaweiSigner();
        signer.sign(request);
        HttpRequest<Buffer> httpRequest = createRequest(url, httpMethod);
        Map<String, String> requestHeaders = request.getHeaders();

        for (String key : requestHeaders.keySet()) {
            if (!key.equalsIgnoreCase("Content-Length")) {
                String value = requestHeaders.get(key);
                httpRequest.putHeader(key, new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            }
        }

        return httpRequest;
    }

    public HttpRequest<Buffer> access(String url,
                                      Map<String, String> headers,
                                      InputStream content,
                                      Long contentLength,
                                      HttpMethod httpMethod) throws Exception {
        String body = "";
        if (content != null) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            int length;
            while ((length = content.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            body = result.toString(StandardCharsets.UTF_8);
        }

        return this.access(url, headers, body, httpMethod);
    }
}
