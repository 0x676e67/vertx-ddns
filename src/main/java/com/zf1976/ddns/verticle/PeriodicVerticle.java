package com.zf1976.ddns.verticle;

import com.zf1976.ddns.cache.AbstractMemoryLogCache;
import com.zf1976.ddns.cache.MemoryLogCache;
import com.zf1976.ddns.enums.DnsProviderType;
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
    private final AbstractMemoryLogCache<DnsProviderType, DnsRecordLog> cache = MemoryLogCache.getInstance();

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
            DnsRecordLog recordLog = (DnsRecordLog) logResult.body();
            final var localAsyncMap = vertx.sharedData()
                                           .getLocalAsyncMap(ApiConstants.SHARE_MAP_ID);
            localAsyncMap.compose(shareMap -> shareMap.get(ApiConstants.SOCKJS_WRITE_HANDLER_ID))
                         .compose(writeHandlerId -> this.storeMemoryLog(writeHandlerId, recordLog))
                         .onSuccess(writeHandlerId -> {
                             if (writeHandlerId != null) {
                                 localAsyncMap.compose(shareMap -> shareMap.get(ApiConstants.SOCKJS_SELECT_PROVIDER_TYPE))
                                              .compose(providerType -> {
                                                  try {
                                                      final var dnsProviderType = (DnsProviderType) providerType;
                                                      return Future.succeededFuture(dnsProviderType);
                                                  } catch (Exception e) {
                                                      return Future.failedFuture(e.getMessage());
                                                  }
                                              })
                                              .onSuccess(dnsProviderType -> {
                                                  if (dnsProviderType.check(recordLog.getDnsProviderType())) {
                                                      eventBus.send(writeHandlerId, Json.encode(logResult.body()));
                                                  }
                                              })
                                              .onFailure(err -> log.error(err.getMessage(), err.getCause()));

                             }
                         })
                         .onFailure(err -> log.error(err.getMessage(), err.getCause()));
        });
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {
        final var periodicId = vertx.setPeriodic(20000, id -> {
            vertx.sharedData()
                 .getLocalAsyncMap(ApiConstants.SHARE_MAP_ID)
                 .compose(shareMap -> shareMap.get(ApiConstants.RUNNING_CONFIG_ID)
                                              .compose(v -> {
                                                  if (!(v instanceof Boolean bool && bool)) {
                                                      this.notifyObserver();
                                                      return Future.succeededFuture();
                                                  } else {
                                                      return shareMap.remove(ApiConstants.RUNNING_CONFIG_ID);
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

    protected Future<String> storeMemoryLog(Object writeHandlerId, DnsRecordLog recordLog) {
        String sendId;
        try {
            sendId = (String) writeHandlerId;
            final var completableFuture = this.cache.get(recordLog.getDnsProviderType());
            return Future.fromCompletionStage(completableFuture, vertx.getOrCreateContext())
                         .compose(collection -> {
                             collection.add(recordLog);
                             if (sendId == null) {
                                 return Future.succeededFuture();
                             }
                             return Future.succeededFuture(sendId);
                         });
        } catch (Exception e) {
            return Future.failedFuture(e.getMessage());
        }

    }
}
