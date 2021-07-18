package com.zf1976.ddns.api;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * @author mac
 * @date 2021/7/18
 */
public class AbstractDnsApi {

    protected HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))
            .version(HttpClient.Version.HTTP_1_1)
            .executor(Executors.newSingleThreadExecutor())
            .build();

}
