import com.zf1976.ddns.api.singer.aliyun.MethodType;
import com.zf1976.ddns.api.singer.aliyun.sign.RpcSignatureComposer;
import com.zf1976.ddns.util.ParameterHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;

/**
 * @author mac
 * @date 2021/7/14
 */
public class SingerTest {

    @Test
    public void aliyunSingerTest() throws InterruptedException {
        String url = "https://alidns.aliyuncs.com";
        final var composer = RpcSignatureComposer.getComposer();
        final var queryParam = new HashMap<String, String>();
        queryParam.put("Format", "JSON");
        queryParam.put("AccessKeyId","");
        queryParam.put("Action","DescribeDomainRecords");
        queryParam.put("SignatureMethod","HMAC-SHA1");
        queryParam.put("DomainName", "innas.cn");
        queryParam.put("SignatureNonce", ParameterHelper.getUniqueNonce());
        queryParam.put("SignatureVersion","1.0");
        queryParam.put("Version", "2015-01-09");
        queryParam.put("TypeKeyWord","A");
        queryParam.put("Timestamp",ParameterHelper.getISO8601Time(new Date()));
        final var toUrl = composer.toUrl(
                "",
                url,
                MethodType.GET,
                queryParam);
        final var httpClient = HttpClient.newHttpClient();
        final var request = HttpRequest.newBuilder()
                                       .uri(URI.create(toUrl))
                                       .GET()
                                       .build();

        try {
            final var body = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                                       .body();
            System.out.println(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
