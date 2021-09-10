package com.zf1976.ddns.expression;

import com.zf1976.ddns.enums.LogStatus;
import com.zf1976.ddns.pojo.DnsRecordLog;
import com.zf1976.ddns.util.ParameterHelper;

import java.util.Date;
import java.util.Set;

/**
 * @author mac
 * 2021/8/26 星期四 10:30 下午
 */
public class TemplateExpressionHandler implements ExpressionParser{

    private static final String providerTag = "#provider";
    private static final String sourceIpTag = "#sourceIp";
    private static final String targetIpTag = "#targetIp";
    private static final String timeTag = "#time";
    private static final String statusTag = "#status";
    private static final String domainTag = "#domain";
    private static final Set<String> SET = Set.of(providerTag, sourceIpTag, targetIpTag, timeTag, statusTag, domainTag);

    public TemplateExpressionHandler() {
    }


    @Override
    public String replaceParser(DnsRecordLog dnsRecordLog, String content) {
        final String finalContent = content;
        boolean condition = false;
        for (String tag : SET) {
            if (finalContent.contains(tag)) {
                condition = true;
                break;
            }
        }

        if (!condition) {
            return dnsRecordLog.getContent();
        }

        if (content.contains(providerTag) && dnsRecordLog.getDnsProviderType() != null) {
            content = content.replaceAll(providerTag, dnsRecordLog.getDnsProviderType().name());
        }

        if (content.contains(sourceIpTag) && dnsRecordLog.getSourceIp() != null) {
            content = content.replaceAll(sourceIpTag, dnsRecordLog.getSourceIp());
        }

        if (content.contains(targetIpTag) && dnsRecordLog.getTargetIp() != null) {
            content = content.replaceAll(targetIpTag, dnsRecordLog.getTargetIp());
        }

        if (content.contains(timeTag) && dnsRecordLog.getTimestamp() != null) {
            content = content.replaceAll(timeTag, ParameterHelper.getTime(new Date(dnsRecordLog.getTimestamp())));
        }

        if (content.contains(statusTag) && dnsRecordLog.getLogStatus() != null) {
            content = content .replaceAll(statusTag, dnsRecordLog.getLogStatus().toString());
        }

        if (content.contains(domainTag) && dnsRecordLog.getDomainOrMessage() != null) {
            content = content.replaceAll(domainTag, dnsRecordLog.getDomainOrMessage());
        }

        if (dnsRecordLog.getLogStatus() == LogStatus.ERROR) {
            content = "DNS provider: " + dnsRecordLog.getDnsProviderType() +
                    "\nStatus: " + dnsRecordLog.getLogStatus() +
                    "\nMessage: " + dnsRecordLog.getDomainOrMessage();
        }

        return content;
    }

}