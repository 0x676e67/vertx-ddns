package com.zf1976.ddns.verticle.timer;

/**
 * @author mac
 * 2021/8/11 星期三 9:45 下午
 */
public interface Subject {

    void addObserver(Observer obj);

    void deleteObserver(Observer obj);

    void notifyObserver();

}
