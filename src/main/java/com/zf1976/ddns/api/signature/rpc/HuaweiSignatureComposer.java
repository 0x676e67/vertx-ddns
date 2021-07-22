package com.zf1976.ddns.api.signature.rpc;

import com.zf1976.ddns.api.enums.MethodType;

import java.util.Arrays;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/22
 */
public class HuaweiSignatureComposer implements RpcAPISignatureComposer {


    @Override
    public String composeStringToSign(MethodType method, Map<String, Object> queries) {
        final var keySet = queries.keySet()
                                  .toArray(new String[]{});
        Arrays.sort(keySet);
        for (String key : keySet) {

        }
        return null;
    }

    @Override
    public String toUrl(String accessKeySecret, String urlPattern, MethodType methodType, Map<String, Object> queries) {
        return null;
    }

    @Override
    public String signatureMethod() {
        return null;
    }

    @Override
    public String getSignerVersion() {
        return null;
    }
}
