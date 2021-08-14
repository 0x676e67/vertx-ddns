package com.zf1976.ddns.verticle;

import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.util.LogUtil;
import com.zf1976.ddns.verticle.timer.AbstractDnsRecordSubject;
import com.zf1976.ddns.verticle.timer.DnsRecordObserver;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author mac
 * 2021/8/9 星期一 11:22 下午
 */
public class PeriodicVerticle extends AbstractDnsRecordSubject {

    private final Logger log = LogManager.getLogger("[PeriodicVerticle]");
    private static final long DEFAULT_PERIODIC_TIME = 5*60*1000;

    public PeriodicVerticle(DnsRecordObserver observer) {
        this.addObserver(observer);
    }

    public PeriodicVerticle(List<DnsRecordObserver> observers) {
        if (!CollectionUtil.isEmpty(observers)) {
            for (DnsRecordObserver observer : observers) {
                this.addObserver(observer);
            }
        }
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.eventBus()
             .consumer(ApiConstants.CONFIG_SUBJECT_ADDRESS, log::info);
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {
        final var periodicId = vertx.setPeriodic(DEFAULT_PERIODIC_TIME, event -> {
            this.notifyObserver();
        });
        context.put(ApiConstants.CONFIG_PERIODIC_ID, periodicId);
    }

    @Override
    public void stop() throws Exception {
        for (DnsRecordObserver observer : this.observers) {
            this.removeObserver(observer);
        }
        final var rawPeriodicId = context.get(ApiConstants.CONFIG_PERIODIC_ID);
        Long periodicId = (Long) rawPeriodicId;
        if (vertx.cancelTimer(periodicId)) {
            log.info("cancel the PeriodicVerticle deployment and cancel the timer!");
        }

    }
}
