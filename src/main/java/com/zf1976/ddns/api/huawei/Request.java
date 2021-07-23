package com.zf1976.ddns.api.huawei;

import com.zf1976.ddns.util.ApiURLEncoder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
public class Request {
    private final Map<String, String> headers = new Hashtable<>();
    private final Map<String, List<String>> queryString = new Hashtable<>();
    private String key = null;
    private String secret = null;
    private String method = null;
    private String url = null;
    private String body = null;
    private String fragment = null;

    public Request() {
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String appKey) throws Exception {
        if (null != appKey && !appKey.trim()
                                     .isEmpty()) {
            this.key = appKey;
        } else {
            throw new Exception("appKey can not be empty");
        }
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String appSecret) throws Exception {
        if (null != appSecret && !appSecret.trim()
                                           .isEmpty()) {
            this.secret = appSecret;
        } else {
            throw new Exception("appSecret can not be empty");
        }
    }

    public HttpMethodName getMethod() {
        return HttpMethodName.valueOf(this.method.toUpperCase());
    }

    public void setMethod(String method) throws Exception {
        if (null == method) {
            throw new Exception("method can not be empty");
        } else if (!method.equalsIgnoreCase("post") && !method.equalsIgnoreCase("put") && !method.equalsIgnoreCase("patch") && !method.equalsIgnoreCase("delete") && !method.equalsIgnoreCase("get") && !method.equalsIgnoreCase("options") && !method.equalsIgnoreCase("head")) {
            throw new Exception("unsupported method");
        } else {
            this.method = method;
        }
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setAppKey(String appKey) throws Exception {
        if (null != appKey && !appKey.trim()
                                     .isEmpty()) {
            this.key = appKey;
        } else {
            throw new Exception("appKey can not be empty");
        }
    }

    public void setAppSecret(String appSecret) throws Exception {
        if (null != appSecret && !appSecret.trim()
                                           .isEmpty()) {
            this.secret = appSecret;
        } else {
            throw new Exception("appSecret can not be empty");
        }
    }

    public String getUrl() {
        StringBuilder uri = new StringBuilder(this.url);
        if (this.queryString.size() > 0) {
            uri.append("?");
            int loop = 0;

            for (Map.Entry<String, List<String>> entry : this.queryString.entrySet()) {
                for (var var5 = entry.getValue()
                                     .iterator(); var5.hasNext(); ++loop) {
                    String value = var5.next();
                    if (loop > 0) {
                        uri.append("&");
                    }

                    uri.append(ApiURLEncoder.huaweiPercentEncode(entry.getKey(), false));
                    uri.append("=");
                    uri.append(ApiURLEncoder.huaweiPercentEncode(value, false));
                }
            }
        }

        if (this.fragment != null) {
            uri.append("#");
            uri.append(this.fragment);
        }

        return uri.toString();
    }

    public void setUrl(String url) throws Exception {
        if (null != url && !url.trim()
                               .isEmpty()) {
            int i = url.indexOf(35);
            if (i >= 0) {
                url = url.substring(0, i);
            }

            i = url.indexOf(63);
            if (i >= 0) {
                String query = url.substring(i + 1);
                String[] var4 = query.split("&");

                for (String item : var4) {
                    String[] spl = item.split("=", 2);
                    String key = spl[0];
                    String value = "";
                    if (spl.length > 1) {
                        value = spl[1];
                    }

                    if (!key.trim()
                            .isEmpty()) {
                        key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                        value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                        this.addQueryStringParam(key, value);
                    }
                }

                url = url.substring(0, i);
            }

            this.url = url;
        } else {
            throw new Exception("url can not be empty");
        }
    }

    public String getPath() {
        String url = this.url;
        int i = url.indexOf("://");
        if (i >= 0) {
            url = url.substring(i + 3);
        }

        i = url.indexOf(47);
        return i >= 0 ? url.substring(i) : "/";
    }

    public String getHost() {
        String url = this.url;
        int i = url.indexOf("://");
        if (i >= 0) {
            url = url.substring(i + 3);
        }

        i = url.indexOf(47);
        if (i >= 0) {
            url = url.substring(0, i);
        }

        return url;
    }

    public void addQueryStringParam(String name, String value) {
        this.queryString.computeIfAbsent(name, k -> new ArrayList<>())
                        .add(value);
    }

    public Map<String, List<String>> getQueryStringParams() {
        return this.queryString;
    }

    public String getFragment() {
        return this.fragment;
    }

    public void setFragment(String fragment) throws Exception {
        if (null != fragment && !fragment.trim()
                                         .isEmpty()) {
            this.fragment = URLEncoder.encode(fragment, StandardCharsets.UTF_8);
        } else {
            throw new Exception("fragment can not be empty");
        }
    }

    public void addHeader(String name, String value) {
        if (null != name && !name.trim()
                                 .isEmpty()) {
            this.headers.put(name, value);
        }
    }
}
