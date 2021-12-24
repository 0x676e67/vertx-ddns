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

package com.zf1976.ddns.api.signer.client;

import com.zf1976.ddns.api.signer.HuaweiRequest;
import com.zf1976.ddns.api.signer.service.HuaweiAccessService;
import com.zf1976.ddns.api.signer.service.HuaweiAccessServiceImpl;
import com.zf1976.ddns.enums.HttpMethod;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
@SuppressWarnings("DuplicatedCode")
public class HuaweiClientSigner {

    public HuaweiClientSigner() {
    }

    public static HttpRequestBase sign(HuaweiRequest request) throws Exception {
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

    public static HttpRequestBase put(String ak,
                                      String sk,
                                      String requestUrl,
                                      Map<String, String> headers,
                                      String putBody) throws Exception {
        HuaweiAccessService<HttpRequestBase> accessService = new HuaweiAccessServiceImpl(ak, sk);
        HttpMethod httpMethod = HttpMethod.PUT;
        if (putBody == null) {
            putBody = "";
        }

        return accessService.access(requestUrl, headers, putBody, httpMethod);
    }

    public static HttpRequestBase patch(String ak,
                                        String sk,
                                        String requestUrl,
                                        Map<String, String> headers,
                                        String body) throws Exception {
        HuaweiAccessService<HttpRequestBase> accessService = new HuaweiAccessServiceImpl(ak, sk);
        HttpMethod httpMethod = HttpMethod.PATCH;
        if (body == null) {
            body = "";
        }

        return accessService.access(requestUrl, headers, body, httpMethod);
    }

    public static HttpRequestBase delete(String ak,
                                         String sk,
                                         String requestUrl,
                                         Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequestBase> accessService = new HuaweiAccessServiceImpl(ak, sk);
        HttpMethod httpMethod = HttpMethod.DELETE;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase get(String ak,
                                      String sk,
                                      String requestUrl,
                                      Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequestBase> accessService = new HuaweiAccessServiceImpl(ak, sk);
        HttpMethod httpMethod = HttpMethod.GET;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase post(String ak,
                                       String sk,
                                       String requestUrl,
                                       Map<String, String> headers,
                                       String postbody) throws Exception {
        HuaweiAccessService<HttpRequestBase> accessService = new HuaweiAccessServiceImpl(ak, sk);
        if (postbody == null) {
            postbody = "";
        }

        HttpMethod httpMethod = HttpMethod.POST;
        return accessService.access(requestUrl, headers, postbody, httpMethod);
    }

    public static HttpRequestBase head(String ak,
                                       String sk,
                                       String requestUrl,
                                       Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequestBase> accessService = new HuaweiAccessServiceImpl(ak, sk);
        HttpMethod httpMethod = HttpMethod.HEAD;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase options(String ak,
                                          String sk,
                                          String requestUrl,
                                          Map<String, String> headers) throws Exception {
        HuaweiAccessService<HttpRequestBase> accessService = new HuaweiAccessServiceImpl(ak, sk);
        HttpMethod httpMethod = HttpMethod.OPTIONS;
        return accessService.access(requestUrl, headers, httpMethod);
    }


}
