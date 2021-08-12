package com.zf1976.ddns.verticle;

import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.verticle.timer.AbstractDnsRecordSubject;
import com.zf1976.ddns.verticle.timer.DnsRecordObserver;
import io.vertx.core.Context;
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
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(50000, periodicId -> {
            final var o = context.get(ApiConstants.CONFIG_PERIODIC_ID);
            if (Objects.isNull(o)) {
                context.put(ApiConstants.CONFIG_PERIODIC_ID, periodicId);
            }
            this.notifyObserver();
            log.info("Update the domain name record once");
        });
    }

    @Override
    public void stop() throws Exception {
        final var o = context.get(ApiConstants.CONFIG_PERIODIC_ID);
        Long periodicId = (Long) o;
        vertx.cancelTimer(periodicId);
    }
}
