package com.zf1976.ddns.api;

import com.zf1976.ddns.api.auth.DnsApiCredentials;
import com.zf1976.ddns.api.auth.TokenCredentials;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * cloudflare DNS
 *
 * @author ant
 * Create by Ant on 2021/7/17 1:24 上午
 */
@SuppressWarnings({"FieldCanBeLocal"})
public class CloudflareDnsApi extends AbstractDnsApi{

    private final String api = "https://api.cloudflare.com/client/v4/zones";
    private final String zoneId;
    private final DnsApiCredentials dnsApiCredentials;

    public CloudflareDnsApi(String token) {
        this(new TokenCredentials(token));
    }


    public CloudflareDnsApi(DnsApiCredentials dnsApiCredentials) {
        final var request = HttpRequest.newBuilder()
                                       .GET()
                                       .uri(URI.create(api))
                                       .header("Authorization","Bearer " + dnsApiCredentials.getAccessKeySecret())
                                       .build();
        try {
            final var body = super.httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            final var result = JsonObject.mapFrom(Json.decodeValue(body))
                                         .getJsonArray("result")
                                         .getList()
                                         .get(0);
            @SuppressWarnings("unchecked") Map<String, String> map = (Map<String, String>) result;
            this.zoneId = map.get("id");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
        this.dnsApiCredentials = dnsApiCredentials;
    }

    public Object findDnsRecords(String type) {
        final var queryParam = getQueryParam(type);
        final var url = this.toQueryParam(queryParam);
        final var request = HttpRequest.newBuilder()
                                       .GET()
                                       .uri(URI.create(url))
                                       .header("Authorization", this.getBearerToken())
                                       .build();
        return this.sendRequest(request);
    }

    private Object sendRequest(HttpRequest request) {
        try {
            return super.httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getBearerToken() {
        final var token = this.dnsApiCredentials.getAccessKeySecret();
        return "Bearer " + token;
    }

    private String toQueryParam(Map<String, String> queryParam) {
        final var query = new StringBuilder();
        final var array = queryParam.keySet().toArray(new String[]{});
        for (String key : array) {
            query.append("&")
                 .append(key)
                 .append("=")
                 .append(queryParam.get(key));
        }
        return this.api + "/" + this.zoneId + "?" + query.substring(1);
    }

    public Map<String, String> getQueryParam(String type) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("match", "all");
        queryParam.put("type", type);
        queryParam.put("per_page", "100");
        return queryParam;
    }

}
