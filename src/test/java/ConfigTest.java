import com.zf1976.ddns.annotation.YamlPrefix;
import com.zf1976.ddns.property.AliyunDnsProperties;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * @author mac
 * @date 2021/7/6
 */
public class ConfigTest {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();
        vertx.setPeriodic(1000, id -> {
            System.out.println(id);
        });
    }

    @Test
    public void yamlTest() {
        final var yaml = new Yaml();
        final var string = yaml.load(ConfigTest.class.getClassLoader()
                                                   .getResourceAsStream("config.yaml"));
        final var aliyun = JsonObject.mapFrom(string)
                                     .getJsonObject("aliyun")
                                     .mapTo(AliyunDnsProperties.class);
        System.out.println(aliyun);
        final var annotation = AliyunDnsProperties.class.getAnnotation(YamlPrefix.class);
        System.out.println(annotation.value());
    }
}
