import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WebClientTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(new VertxOptions());
        WebClientOptions webClientOptions = new WebClientOptions()
                .setDefaultPort(8080);
        WebClient webClient = WebClient.create(vertx, webClientOptions);
        webClient.get("http://github.com")
                .send()
                .onSuccess(response -> System.out
                        .println("Received response with data result" + response.body().toString()))
                .onFailure(err ->
                        System.out.println("Something went wrong " + err.getMessage())
                );
    }

    @Test
    public void requestTest() throws MalformedObjectNameException {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        System.out.println(beanServer);

    }
}
