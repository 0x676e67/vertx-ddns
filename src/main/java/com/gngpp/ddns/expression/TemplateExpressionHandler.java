/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gngpp.ddns.expression;

import com.gngpp.ddns.pojo.DnsRecordLog;
import com.gngpp.ddns.util.ParameterHelper;
import com.gngpp.ddns.enums.LogStatus;

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
            content = "- DNS provider: " + dnsRecordLog.getDnsProviderType() +
                    "\n- Status: " + dnsRecordLog.getLogStatus() +
                    "\n- Message: " + dnsRecordLog.getDomainOrMessage();
        }

        return content;
    }

}