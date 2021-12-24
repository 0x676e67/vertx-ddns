/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zf1976.ddns.api.signer.service;

import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.api.signer.HuaweiSigner;
import com.zf1976.ddns.enums.HttpMethod;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author mac
 * 2021/7/24
 */
@SuppressWarnings("DuplicatedCode")
public class HuaweiAccessServiceImpl extends HuaweiAccessService<HttpRequestBase> {

    public HuaweiAccessServiceImpl(String ak, String sk) {
        super(ak, sk);
    }

    @SuppressWarnings("SameParameterValue")
    private static HttpRequestBase createRequest(String url, Header header, String content, HttpMethod httpMethod) {
        HttpRequestBase httpRequest;
        StringEntity entity;
        if (httpMethod == HttpMethod.POST) {
            HttpPost postMethod = new HttpPost(url);
            if (content != null) {
                entity = new StringEntity(content, StandardCharsets.UTF_8);
                postMethod.setEntity(entity);
            }

            httpRequest = postMethod;
        } else if (httpMethod == HttpMethod.PUT) {
            HttpPut putMethod = new HttpPut(url);
            httpRequest = putMethod;
            if (content != null) {
                entity = new StringEntity(content, StandardCharsets.UTF_8);
                putMethod.setEntity(entity);
            }
        } else if (httpMethod == HttpMethod.PATCH) {
            HttpPatch patchMethod = new HttpPatch(url);
            httpRequest = patchMethod;
            if (content != null) {
                entity = new StringEntity(content, StandardCharsets.UTF_8);
                patchMethod.setEntity(entity);
            }
        } else if (httpMethod == HttpMethod.GET) {
            httpRequest = new HttpGet(url);
        } else if (httpMethod == HttpMethod.DELETE) {
            httpRequest = new HttpDelete(url);
        } else if (httpMethod == HttpMethod.OPTIONS) {
            httpRequest = new HttpOptions(url);
        } else {
            if (httpMethod != HttpMethod.HEAD) {
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
                                  HttpMethod httpMethod) throws Exception {
        HuaweiRequest request = this.newRequest(url, httpMethod);

        for (String k : headers.keySet()) {
            request.addHeader(k, headers.get(k));
        }

        request.setBody(content);
        HuaweiSigner signer = new HuaweiSigner();
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
