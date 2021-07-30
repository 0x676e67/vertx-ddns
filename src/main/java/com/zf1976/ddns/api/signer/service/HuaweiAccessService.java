package com.zf1976.ddns.api.signer.service;

import com.zf1976.ddns.api.enums.MethodType;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.InputStream;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
public abstract class HuaweiAccessService {
    protected String ak;
    protected String sk;

    public HuaweiAccessService(String ak, String sk) {
        this.ak = ak;
        this.sk = sk;
    }

    public abstract HttpRequestBase access(String var1,
                                           Map<String, String> var2,
                                           InputStream var3,
                                           Long var4,
                                           MethodType var5) throws Exception;

    public abstract HttpRequestBase access(String var1,
                                           Map<String, String> var2,
                                           String var3,
                                           MethodType var4) throws Exception;

    public HttpRequestBase access(String url, Map<String, String> header, MethodType httpMethod) throws Exception {
        return this.access(url, header, null, 0L, httpMethod);
    }

    public HttpRequestBase access(String url,
                                  InputStream content,
                                  Long contentLength,
                                  MethodType httpMethod) throws Exception {
        return this.access(url, null, content, contentLength, httpMethod);
    }

    public HttpRequestBase access(String url, MethodType httpMethod) throws Exception {
        return this.access(url, null, null, 0L, httpMethod);
    }

    public String getAk() {
        return this.ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return this.sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }
}