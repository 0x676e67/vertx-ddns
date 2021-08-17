package com.zf1976.ddns.pojo;

import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.enums.LogStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ant
 * Create by Ant on 2021/8/17 1:52 AM
 */
@SuppressWarnings("RedundantCast")
public class DnsRecordLog implements Serializable {

    private DnsProviderType dnsProviderType;

    private String content;

    private Long timestamp;

    public DnsRecordLog() {

    }

    private DnsRecordLog(DnsProviderType dnsProviderType, String domain, String ip, LogStatus logStatus) {
        this.dnsProviderType = dnsProviderType;
        switch (logStatus) {
            case ROW -> this.content = "域名：" + domain + " 没有发生变化, IP：" + ip;
            case CREATE -> this.content = "新增域名解析：" + domain + " 成功！IP：" + ip;
            case CREATE_FAIL -> this.content = "新增域名解析：" + domain + " 失败！";
            case MODIFY -> this.content = "更新域名解析：" + domain + " 成功！IP：" + ip;
            case MODIFY_FAIL -> this.content = "更新域名解析：" + domain + " 失败！";
            default -> throw new UnsupportedOperationException("Unsupported log status: " + logStatus.name());
        }
        this.timestamp = new Date().getTime();
    }

    public static DnsRecordLog rawLog(DnsProviderType dnsProviderType, String domain, String ip) {
        return new DnsRecordLog(dnsProviderType, domain, ip, LogStatus.ROW);
    }

    public static DnsRecordLog createLog(DnsProviderType dnsProviderType, String domain, String ip) {
        return new DnsRecordLog(dnsProviderType, domain, ip, LogStatus.CREATE);
    }

    public static DnsRecordLog createFailLog(DnsProviderType dnsProviderType, String domain) {
        return new DnsRecordLog(dnsProviderType, domain, (String) null, LogStatus.CREATE_FAIL);
    }

    public static DnsRecordLog modifyLog(DnsProviderType dnsProviderType, String domain, String ip) {
        return new DnsRecordLog(dnsProviderType, domain, ip, LogStatus.MODIFY);
    }

    public static DnsRecordLog modifyFailLog(DnsProviderType dnsProviderType, String domain) {
        return new DnsRecordLog(dnsProviderType, domain, (String) null, LogStatus.MODIFY_FAIL);
    }

    public DnsProviderType getDnsProviderType() {
        return dnsProviderType;
    }

    public DnsRecordLog setDnsProviderType(DnsProviderType dnsProviderType) {
        this.dnsProviderType = dnsProviderType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public DnsRecordLog setContent(String content) {
        this.content = content;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public DnsRecordLog setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "DnsRecordLog{" +
                "dnsProviderType=" + dnsProviderType +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
