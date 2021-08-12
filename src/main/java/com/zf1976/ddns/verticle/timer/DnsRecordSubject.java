package com.zf1976.ddns.verticle.timer;

/**
 * @author mac
 * 2021/8/11 星期三 9:45 下午
 */
public interface DnsRecordSubject {

    void addObserver(DnsRecordObserver obj);

    void deleteObserver(DnsRecordObserver obj);

    void notifyObserver();

}
