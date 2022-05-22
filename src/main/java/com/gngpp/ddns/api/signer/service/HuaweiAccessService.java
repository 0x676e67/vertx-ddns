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

package com.gngpp.ddns.api.signer.service;

import com.gngpp.ddns.api.auth.BasicCredentials;
import com.gngpp.ddns.api.signer.HuaweiRequest;
import com.gngpp.ddns.enums.HttpMethod;

import java.io.InputStream;
import java.util.Map;

/**
 * @author mac
 * 2021/7/24
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
