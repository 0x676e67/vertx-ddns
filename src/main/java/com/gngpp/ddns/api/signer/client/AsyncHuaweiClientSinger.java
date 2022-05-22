/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
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

package com.gngpp.ddns.api.signer.client;

import com.gngpp.ddns.api.signer.HuaweiRequest;
import com.gngpp.ddns.api.signer.service.AsyncHuaweiAccessServiceImpl;
import com.gngpp.ddns.api.signer.service.HuaweiAccessService;
import com.gngpp.ddns.enums.HttpMethod;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.util.Map;

/**
 * @author ant
 * Create by Ant on 2021/8/6 9:03 PM
 */
@SuppressWarnings("DuplicatedCode")
public class AsyncHuaweiClientSinger {

    private static WebClient webClient;

    public AsyncHuaweiClientSinger() {
    }

    public static void initClient(WebClient webClient) {
        AsyncHuaweiClientSinger.webClient = webClient;
    }

    public static HttpRequest<Buffer> signAsync(HuaweiRequest request) throws Exception {
        String appKey = request.getKey();
        String appSecret = request.getSecret();
        String url = request.getUrl();
        String body = request.getBody();
        Map<String, String> headers = request.getHeaders();
        return switch (request.getMethod()) {
            case GET -> get(appKey, appSecret, url, headers);
            case POST -> post(appKey, appSecret, url, headers, body);
            case PUT -> put(appKey, appSecret, url, headers, body);
            case PATCH -> patch(appKey, appSecret, url, headers, body);
            case DELETE -> delete(appKey, appSecret, url, headers);
            case HEAD -> head(appKey, appSecret, url, headers);
            case OPTIONS -> options(appKey, appSecret, url, headers);
        };
    }

    public static HttpRequest<Buffer> put(String ak,
                                          String sk,
                                          String requestUrl,
                                          Map<String, String> headers,
                                          String putBody) throws Exception {
        HuaweiAccessService<HttpRequest<Buffer>> accessService = new AsyncHuaweiAccessServiceImpl(ak, sk, webClient);
        HttpMethod httpMethod = HttpMethod.PUT;
        if (putBody == null) {
            putBody = "";
        }

        return accessService.access(requestUrl, headers, putBody, httpMethod);
    }

    public static HttpRequest<Buffer> patch(String ak,
                                            String sk,
                                            String requestUrl,
                                            Map<String, String> headers,
                                            String body) throws Exception {
        HuaweiAccessService<HttpRequest<Buffer>> accessService = new AsyncHuaweiAccessServiceImpl(ak, sk, webClient);
        HttpMethod httpMethod = HttpMethod.PATCH;
        if (body == null) {
            body = "";
        }

        return accessService.access(requestUrl, headers, body, httpMethod);
    }

    public static HttpRequest<Buffer> delete(String ak,
                                             String sk,
                                             String requestUrl,
                                             Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequest<Buffer>> accessService = new AsyncHuaweiAccessServiceImpl(ak, sk, webClient);
        HttpMethod httpMethod = HttpMethod.DELETE;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequest<Buffer> get(String ak,
                                          String sk,
                                          String requestUrl,
                                          Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequest<Buffer>> accessService = new AsyncHuaweiAccessServiceImpl(ak, sk, webClient);
        HttpMethod httpMethod = HttpMethod.GET;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequest<Buffer> post(String ak,
                                           String sk,
                                           String requestUrl,
                                           Map<String, String> headers,
                                           String postbody) throws Exception {
        HuaweiAccessService<HttpRequest<Buffer>> accessService = new AsyncHuaweiAccessServiceImpl(ak, sk, webClient);
        if (postbody == null) {
            postbody = "";
        }

        HttpMethod httpMethod = HttpMethod.POST;
        return accessService.access(requestUrl, headers, postbody, httpMethod);
    }

    public static HttpRequest<Buffer> head(String ak,
                                           String sk,
                                           String requestUrl,
                                           Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequest<Buffer>> accessService = new AsyncHuaweiAccessServiceImpl(ak, sk, webClient);
        HttpMethod httpMethod = HttpMethod.HEAD;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequest<Buffer> options(String ak,
                                              String sk,
                                              String requestUrl,
                                              Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequest<Buffer>> accessService = new AsyncHuaweiAccessServiceImpl(ak, sk, webClient);
        HttpMethod httpMethod = HttpMethod.OPTIONS;
        return accessService.access(requestUrl, headers, httpMethod);
    }
}
