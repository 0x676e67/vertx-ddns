package com.zf1976.ddns.api.huawei;

import org.apache.http.client.methods.HttpRequestBase;

import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
public class Client {
    public Client() {
    }

    public static HttpRequestBase sign(Request request) throws Exception {
        String appKey = request.getKey();
        String appSecret = request.getSecret();
        String url = request.getUrl();
        String body = request.getBody();
        Map<String, String> headers = request.getHeaders();
        switch (request.getMethod()) {
            case GET:
                return get(appKey, appSecret, url, headers);
            case POST:
                return post(appKey, appSecret, url, headers, body);
            case PUT:
                return put(appKey, appSecret, url, headers, body);
            case PATCH:
                return patch(appKey, appSecret, url, headers, body);
            case DELETE:
                return delete(appKey, appSecret, url, headers);
            case HEAD:
                return head(appKey, appSecret, url, headers);
            case OPTIONS:
                return options(appKey, appSecret, url, headers);
            default:
                throw new IllegalArgumentException(String.format("unsupported method:%s", request.getMethod()
                                                                                                 .name()));
        }
    }

    public static HttpRequestBase put(String ak,
                                      String sk,
                                      String requestUrl,
                                      Map<String, String> headers,
                                      String putBody) throws Exception {
        AccessService accessService = new AccessServiceImpl(ak, sk);
        HttpMethodName httpMethod = HttpMethodName.PUT;
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
        AccessService accessService = new AccessServiceImpl(ak, sk);
        HttpMethodName httpMethod = HttpMethodName.PATCH;
        if (body == null) {
            body = "";
        }

        return accessService.access(requestUrl, headers, body, httpMethod);
    }

    public static HttpRequestBase delete(String ak,
                                         String sk,
                                         String requestUrl,
                                         Map<String, String> headers) throws Exception {
        AccessService accessService = new AccessServiceImpl(ak, sk);
        HttpMethodName httpMethod = HttpMethodName.DELETE;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase get(String ak,
                                      String sk,
                                      String requestUrl,
                                      Map<String, String> headers) throws Exception {
        AccessService accessService = new AccessServiceImpl(ak, sk);
        HttpMethodName httpMethod = HttpMethodName.GET;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase post(String ak,
                                       String sk,
                                       String requestUrl,
                                       Map<String, String> headers,
                                       String postbody) throws Exception {
        AccessService accessService = new AccessServiceImpl(ak, sk);
        if (postbody == null) {
            postbody = "";
        }

        HttpMethodName httpMethod = HttpMethodName.POST;
        return accessService.access(requestUrl, headers, postbody, httpMethod);
    }

    public static HttpRequestBase head(String ak,
                                       String sk,
                                       String requestUrl,
                                       Map<String, String> headers) throws Exception {
        AccessService accessService = new AccessServiceImpl(ak, sk);
        HttpMethodName httpMethod = HttpMethodName.HEAD;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase options(String ak,
                                          String sk,
                                          String requestUrl,
                                          Map<String, String> headers) throws Exception {
        AccessService accessService = new AccessServiceImpl(ak, sk);
        HttpMethodName httpMethod = HttpMethodName.OPTIONS;
        return accessService.access(requestUrl, headers, httpMethod);
    }


}
