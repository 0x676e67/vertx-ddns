package com.zf1976.ddns.api.signer.service;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.enums.HttpMethod;
import com.zf1976.ddns.api.signer.HuaweiRequest;

import java.io.InputStream;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
public abstract class HuaweiAccessService<T> {
    protected String ak;
    protected String sk;

    public HuaweiAccessService(String ak, String sk) {
        this.ak = ak;
        this.sk = sk;
    }

    public abstract T access(String var1,
                             Map<String, String> var2,
                             InputStream var3,
                             Long var4,
                             HttpMethod var5) throws Exception;

    public abstract T access(String var1,
                             Map<String, String> var2,
                             String var3,
                             HttpMethod var4) throws Exception;

    public T access(String url, Map<String, String> header, HttpMethod httpMethod) throws Exception {
        return this.access(url, header, null, 0L, httpMethod);
    }

    public T access(String url,
                    InputStream content,
                    Long contentLength,
                    HttpMethod httpMethod) throws Exception {
        return this.access(url, null, content, contentLength, httpMethod);
    }

    public T access(String url, HttpMethod httpMethod) throws Exception {
        return this.access(url, null, null, 0L, httpMethod);
    }

    protected String getAk() {
        return this.ak;
    }

    protected void setAk(String ak) {
        this.ak = ak;
    }

    protected String getSk() {
        return this.sk;
    }

    protected void setSk(String sk) {
        this.sk = sk;
    }

    protected HuaweiRequest newRequest(String url, HttpMethod httpMethod) {
        return HuaweiRequest.newBuilder(new BasicCredentials(this.ak, this.sk))
                            .setMethod(httpMethod)
                            .setUrl(url);
    }
}
