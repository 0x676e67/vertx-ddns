package com.zf1976.ddns.verticle.timer;

import io.vertx.core.AbstractVerticle;

import java.util.LinkedList;
import java.util.List;

/**
 * @author ant
 * Create by Ant on 2021/8/11 11:10 PM
 */
public abstract class AbstractDnsRecordSubject extends AbstractVerticle implements DnsRecordSubject {

    protected final List<DnsRecordObserver> observers = new LinkedList<>();

    @Override
    public void addObserver(DnsRecordObserver obj) {
        this.observers.add(obj);
    }

    @Override
    public void deleteObserver(DnsRecordObserver obj) {
        this.observers.remove(obj);
    }

    @Override
    public void notifyObserver() {
        for (DnsRecordObserver observer : this.observers) {
            observer.update();
        }
    }
}
