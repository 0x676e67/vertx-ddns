package com.zf1976.ddns.api.signer;

import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signer.service.HuaweiAccessService;
import com.zf1976.ddns.api.signer.service.HuaweiAccessServiceImpl;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
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
        HuaweiAccessService accessService = new HuaweiAccessServiceImpl(ak, sk);
        MethodType httpMethod = MethodType.PUT;
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
        HuaweiAccessService accessService = new HuaweiAccessServiceImpl(ak, sk);
        MethodType httpMethod = MethodType.PATCH;
        if (body == null) {
            body = "";
        }

        return accessService.access(requestUrl, headers, body, httpMethod);
    }

    public static HttpRequestBase delete(String ak,
                                         String sk,
                                         String requestUrl,
                                         Map<String, String> headers) throws Exception {
        HuaweiAccessService accessService = new HuaweiAccessServiceImpl(ak, sk);
        MethodType httpMethod = MethodType.DELETE;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase get(String ak,
                                      String sk,
                                      String requestUrl,
                                      Map<String, String> headers) throws Exception {
        HuaweiAccessService accessService = new HuaweiAccessServiceImpl(ak, sk);
        MethodType httpMethod = MethodType.GET;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase post(String ak,
                                       String sk,
                                       String requestUrl,
                                       Map<String, String> headers,
                                       String postbody) throws Exception {
        HuaweiAccessService accessService = new HuaweiAccessServiceImpl(ak, sk);
        if (postbody == null) {
            postbody = "";
        }

        MethodType httpMethod = MethodType.POST;
        return accessService.access(requestUrl, headers, postbody, httpMethod);
    }

    public static HttpRequestBase head(String ak,
                                       String sk,
                                       String requestUrl,
                                       Map<String, String> headers) throws Exception {
        HuaweiAccessService accessService = new HuaweiAccessServiceImpl(ak, sk);
        MethodType httpMethod = MethodType.HEAD;
        return accessService.access(requestUrl, headers, httpMethod);
    }

    public static HttpRequestBase options(String ak,
                                          String sk,
                                          String requestUrl,
                                          Map<String, String> headers) throws Exception {
        HuaweiAccessService accessService = new HuaweiAccessServiceImpl(ak, sk);
        MethodType httpMethod = MethodType.OPTIONS;
        return accessService.access(requestUrl, headers, httpMethod);
    }


}
