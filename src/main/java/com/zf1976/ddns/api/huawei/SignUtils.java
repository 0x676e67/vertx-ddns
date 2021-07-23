package com.zf1976.ddns.api.huawei;

import com.zf1976.ddns.api.huawei.vo.SignResult;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/24
 */
public class SignUtils {

    public SignUtils() {
    }

    public static SignResult sign(Request request) throws Exception {
        SignResult result = new SignResult();
        HttpRequestBase signedRequest = Client.sign(request);
        Header[] headers = signedRequest.getAllHeaders();
        Map<String, String> headerMap = new HashMap<>();

        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }

        result.setUrl(signedRequest.getURI()
                                   .toURL());
        result.setHeaders(headerMap);
        return result;
    }
}
