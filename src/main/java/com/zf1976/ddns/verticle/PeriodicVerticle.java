package com.zf1976.ddns.verticle;

import com.zf1976.ddns.pojo.DnsRecordLog;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.verticle.codec.DnsRecordLogMessageCodec;
import com.zf1976.ddns.verticle.timer.AbstractDnsRecordSubject;
import com.zf1976.ddns.verticle.timer.DnsRecordObserver;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author mac
 * 2021/8/9 星期一 11:22 下午
 */
public class PeriodicVerticle extends AbstractDnsRecordSubject {

    private final Logger log = LogManager.getLogger("[PeriodicVerticle]");
    private static final long DEFAULT_PERIODIC_TIME = 5 * 60 * 1000;
    private final DnsRecordLogMessageCodec dnsRecordLogMessageCodec = new DnsRecordLogMessageCodec();
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
        final var eventBus = vertx.eventBus();
        // custom message codec
        eventBus.registerDefaultCodec(DnsRecordLog.class, this.dnsRecordLogMessageCodec);
        // send dns record resolve log
        eventBus.consumer(ApiConstants.CONFIG_SUBJECT_ADDRESS, logResult -> {
            vertx.sharedData()
                 .getAsyncMap(ApiConstants.SOCKJS_ID, asyncMapAsyncResult -> {
                     if (asyncMapAsyncResult.succeeded()) {
                         asyncMapAsyncResult.result()
                                            .get(ApiConstants.SOCKJS_WRITE_HANDLER_ID)
                                            .onSuccess(writeHandlerId -> {
                                                if (writeHandlerId != null) {
                                                    eventBus.send((String) writeHandlerId, Json.encode(logResult.body()));
                                                }
                                            });
                     } else {
                         log.error(asyncMapAsyncResult.cause()
                                                      .getMessage());
                     }
                 });
             });
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {
        final var periodicId = vertx.setPeriodic(10000, event -> {
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
