import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.google.gson.Gson;
import com.zf1976.ddns.config.ConfigProperty;
import com.zf1976.ddns.service.AliyunDDNSService;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.HttpUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/6
 */
public class DDNSTest {

    private static void logPrint(String functionName, Object result) {
        Gson gson = new Gson();
        System.out.println("-------------------------------" + functionName + "-------------------------------");
        System.out.println(gson.toJson(result));
    }

    @Test
    public void aliyunUpdateTest() {
        final var aliyunDDNSService = new AliyunDDNSService(ConfigProperty.getAliyunDnsProperties());
        final var currentHostIp = HttpUtil.getCurrentHostIp();
        final var describeDomainRecords = aliyunDDNSService.findDescribeDomainRecords("linux.innas.cn");
        if (!CollectionUtil.isEmpty(describeDomainRecords.getDomainRecords())) {
            final var record = describeDomainRecords.getDomainRecords()
                                                    .get(0);
            final var updateDomainRecordResponse = aliyunDDNSService.updateDomainRecord(record.getRecordId(), "linux", currentHostIp);
            logPrint("getDescribeDomainRecordsRequest", updateDomainRecordResponse);
        }
    }

    @Test
    public void aliyunAddTest() {

        AliyunDDNSService ddns = new AliyunDDNSService(ConfigProperty.getAliyunDnsProperties());
        ddns.addDomainRecordResponse("demo.innas.cn", HttpUtil.getCurrentHostIp());

    }

    @Test
    public void aliyunFindTest() {
        final var ddns = new AliyunDDNSService(ConfigProperty.getAliyunDnsProperties());
        final var describeDomainRecords = ddns.findDescribeDomainRecords("demo.innas.cn");
        logPrint("demo", describeDomainRecords.getDomainRecords());
    }
}
