import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.zf1976.ddns.api.AliyunDDNSApiService;
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
    public void aliyunTest() {
        // 设置鉴权参数，初始化客户端
        DefaultProfile profile = DefaultProfile.getProfile(
                // 地域ID
                "cn-qingdao",
                // 您的AccessKey ID
                "LTAI4G4JxmHHtbBJJaQaBC5a",
                // 您的AccessKey Secret
                "bPZaIFSCDW2mhPdyII98ec2GsoLq5y");
        IAcsClient client = new DefaultAcsClient(profile);

        AliyunDDNSApiService ddns = new AliyunDDNSApiService();

        // 查询指定二级域名的最新解析记录
        DescribeDomainRecordsRequest describeDomainRecordsRequest = new DescribeDomainRecordsRequest();
        // 主域名
        describeDomainRecordsRequest.setDomainName("innas.cn");
        // 主机记录
        describeDomainRecordsRequest.setRRKeyWord("linux");
        // 解析记录类型
        describeDomainRecordsRequest.setType("A");
        DescribeDomainRecordsResponse describeDomainRecordsResponse = ddns.describeDomainRecords(describeDomainRecordsRequest, client);
        logPrint("describeDomainRecords",describeDomainRecordsResponse);

        List<DescribeDomainRecordsResponse.Record> domainRecords = describeDomainRecordsResponse.getDomainRecords();
        // 最新的一条解析记录
        if(domainRecords.size() != 0 ){
            DescribeDomainRecordsResponse.Record record = domainRecords.get(0);
            // 记录ID
            String recordId = record.getRecordId();
            // 记录值
            String recordsValue = record.getValue();
            // 当前主机公网IP
            String currentHostIP = HttpUtil.getCurrentHostIp();
            System.out.println("-------------------------------当前主机公网IP为："+currentHostIP+"-------------------------------");
            if(!currentHostIP.equals(recordsValue)){
                // 修改解析记录
                UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
                // 主机记录
                updateDomainRecordRequest.setRR("ddnstest");
                // 记录ID
                updateDomainRecordRequest.setRecordId(recordId);
                // 将主机记录值改为当前主机IP
                updateDomainRecordRequest.setValue(currentHostIP);
                // 解析记录类型
                updateDomainRecordRequest.setType("A");
                UpdateDomainRecordResponse updateDomainRecordResponse = ddns.updateDomainRecord(updateDomainRecordRequest, client);
                logPrint("updateDomainRecord",updateDomainRecordResponse);
            }
        }
    }
}
