package com.zf1976.ddns.verticle;

import com.zf1976.ddns.pojo.DnsRecordLog;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.verticle.codec.DnsRecordLogMessageCodec;
import com.zf1976.ddns.verticle.timer.AbstractDnsRecordSubject;
import com.zf1976.ddns.verticle.timer.DnsRecordObserver;
import io.vertx.core.Future;
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
                 .getLocalAsyncMap(ApiConstants.SHARE_MAP_ID)
                 .compose(shareMap -> shareMap.get(ApiConstants.SOCKJS_WRITE_HANDLER_ID))
                 .onSuccess(v -> {
                     eventBus.send((String) v, Json.encode(logResult.body()));
                 })
                 .onFailure(err -> log.error(err.getMessage(), err.getCause()));
        });
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {
        final var localMap = vertx.sharedData()
                                  .getLocalMap(ApiConstants.SHARE_MAP_ID);
        final var periodicId = vertx.setPeriodic(DEFAULT_PERIODIC_TIME, id -> {
            vertx.sharedData()
                 .getLocalAsyncMap(ApiConstants.SHARE_MAP_ID)
                 .compose(shareMap -> shareMap.get(ApiConstants.RUNNING_CONFIG_ID)
                                              .compose(v -> {
                                                  if (!(v instanceof Boolean bool && bool)) {
                                                      this.notifyObserver();
                                                      return Future.succeededFuture();
                                                  } else {
                                                      final var remove = localMap.remove(ApiConstants.RUNNING_CONFIG_ID);
                                                      return Future.succeededFuture(remove);
                                                  }
                                              }))
                 .onFailure(err -> log.error(err.getMessage(), err.getCause()));
        });
        context.put(ApiConstants.DEFAULT_CONFIG_PERIODIC_ID, periodicId);
    }

    @Override
    public void stop() throws Exception {
        for (DnsRecordObserver observer : this.observers) {
            this.removeObserver(observer);
        }
        final var periodicId = context.get(ApiConstants.DEFAULT_CONFIG_PERIODIC_ID);
        if (vertx.cancelTimer((Long) periodicId)) {
            log.info("cancel the PeriodicVerticle deployment and cancel the timer!");
        }
    }


}
