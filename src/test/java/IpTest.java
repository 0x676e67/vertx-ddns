import com.zf1976.ddns.property.CommonProperties;
import com.zf1976.ddns.util.HttpUtil;
import com.zf1976.ddns.util.PropertyUtil;
import org.junit.jupiter.api.Test;

/**
 * @author mac
 * @date 2021/7/6
 */
public class IpTest {

    @Test
    public void ipApiTest() {
        final var commonProperties = PropertyUtil.getCommonProperties();
        for (String ipApi : commonProperties.getIpApiList()) {
            HttpUtil.getCurrentHostIp(ipApi);
        }
    }
}
