package com.zf1976.ddns.api.signer.service;

import com.zf1976.ddns.api.enums.MethodType;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.InputStream;
import java.util.Map;

/**
 * @author ant
 * Create by Ant on 2021/8/6 1:39 PM
 */
public class AsyncHuaweiAccessServiceImpl extends HuaweiAccessService{
    
    public AsyncHuaweiAccessServiceImpl(String ak, String sk) {
        super(ak, sk);
    }

    @Override
    public HttpRequestBase access(String var1,
                                  Map<String, String> var2,
                                  InputStream var3,
                                  Long var4,
                                  MethodType var5) throws Exception {
        return null;
    }

    @Override
    public HttpRequestBase access(String var1,
                                  Map<String, String> var2,
                                  String var3,
                                  MethodType var4) throws Exception {
        return null;
    }
}
