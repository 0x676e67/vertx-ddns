package com.zf1976.ddns.api.huawei;

import com.zf1976.ddns.api.huawei.signer.Signer;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
public class AccessServiceImpl extends AccessService {

    public AccessServiceImpl(String ak, String sk) {
        super(ak, sk);
    }

    @SuppressWarnings("SameParameterValue")
    private static HttpRequestBase createRequest(String url, Header header, String content, HttpMethodName httpMethod) {
        HttpRequestBase httpRequest;
        StringEntity entity;
        if (httpMethod == HttpMethodName.POST) {
            HttpPost postMethod = new HttpPost(url);
            if (content != null) {
                entity = new StringEntity(content, StandardCharsets.UTF_8);
                postMethod.setEntity(entity);
            }

            httpRequest = postMethod;
        } else if (httpMethod == HttpMethodName.PUT) {
            HttpPut putMethod = new HttpPut(url);
            httpRequest = putMethod;
            if (content != null) {
                entity = new StringEntity(content, StandardCharsets.UTF_8);
                putMethod.setEntity(entity);
            }
        } else if (httpMethod == HttpMethodName.PATCH) {
            HttpPatch patchMethod = new HttpPatch(url);
            httpRequest = patchMethod;
            if (content != null) {
                entity = new StringEntity(content, StandardCharsets.UTF_8);
                patchMethod.setEntity(entity);
            }
        } else if (httpMethod == HttpMethodName.GET) {
            httpRequest = new HttpGet(url);
        } else if (httpMethod == HttpMethodName.DELETE) {
            httpRequest = new HttpDelete(url);
        } else if (httpMethod == HttpMethodName.OPTIONS) {
            httpRequest = new HttpOptions(url);
        } else {
            if (httpMethod != HttpMethodName.HEAD) {
                throw new RuntimeException("Unknown HTTP method name: " + httpMethod);
            }

            httpRequest = new HttpHead(url);
        }

        httpRequest.addHeader(header);
        return httpRequest;
    }

    public HttpRequestBase access(String url,
                                  Map<String, String> headers,
                                  String content,
                                  HttpMethodName httpMethod) throws Exception {
        Request request = new Request();
        request.setAppKey(this.ak);
        request.setAppSecret(this.sk);
        request.setMethod(httpMethod.name());
        request.setUrl(url);

        for (String k : headers.keySet()) {
            request.addHeader(k, headers.get(k));
        }

        request.setBody(content);
        Signer signer = new Signer();
        signer.sign(request);
        HttpRequestBase httpRequestBase = createRequest(url, null, content, httpMethod);
        Map<String, String> requestHeaders = request.getHeaders();

        for (String key : requestHeaders.keySet()) {
            if (!key.equalsIgnoreCase("Content-Length")) {
                String value = requestHeaders.get(key);
                httpRequestBase.addHeader(key, new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            }
        }

        return httpRequestBase;
    }

    public HttpRequestBase access(String url,
                                  Map<String, String> headers,
                                  InputStream content,
                                  Long contentLength,
                                  HttpMethodName httpMethod) throws Exception {
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
