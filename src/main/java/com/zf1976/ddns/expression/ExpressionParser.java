package com.zf1976.ddns.expression;

import com.zf1976.ddns.pojo.DnsRecordLog;

/**
 * @author mac
 * 2021/8/26 星期四 10:26 下午
 */
public interface ExpressionParser {

    String replaceParser(DnsRecordLog dnsRecordLog, String content);
}
