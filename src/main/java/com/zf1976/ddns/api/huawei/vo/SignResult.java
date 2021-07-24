package com.zf1976.ddns.api.huawei.vo;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
public class SignResult {
    private Map<String, String> headers = new HashMap<>();
    private URL url;
    private Map<String, String> parameters = new HashMap<>();
    private InputStream inputStream;

    public SignResult() {
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }


    @Override
    public String toString() {
        return "SignResult{" +
                "headers=" + headers +
                ", url=" + url +
                ", parameters=" + parameters +
                ", inputStream=" + inputStream +
                '}';
    }
}
