package com.zf1976.ddns.verticle;

import com.zf1976.ddns.cache.AbstractMemoryLogCache;
import com.zf1976.ddns.cache.MemoryLogCache;
import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.pojo.DnsRecordLog;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.verticle.codec.DnsRecordLogMessageCodec;
import com.zf1976.ddns.verticle.handler.WebhookHandler;
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
    private final WebhookHandler webhookHandler;

    public PeriodicVerticle(DnsRecordObserver observer, WebhookHandler webhookHandler) {
        this.addObserver(observer);
        this.webhookHandler = webhookHandler;
    }

    public PeriodicVerticle(List<DnsRecordObserver> observers, WebhookHandler webhookHandler) {
        this.webhookHandler = webhookHandler;
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
                         .compose(writeHandlerId -> localAsyncMap.compose(shareMap -> shareMap.get(ApiConstants.SOCKJS_SELECT_PROVIDER_TYPE))
                                                                 .compose(v -> {
                                                                     try {
                                                                         final var dnsProviderType = (DnsProviderType) v;
                                                                         if (recordLog.getDnsProviderType()
                                                                                      .check(dnsProviderType)) {
                                                                             return Future.succeededFuture(writeHandlerId);
                                                                         }
                                                                         return Future.succeededFuture();
                                                                     } catch (Exception e) {
                                                                         return Future.failedFuture(e.getMessage());
                                                                     }
                                                                 }))
                         .onSuccess(writeHandlerId -> {
                             if (writeHandlerId != null) {
                                 eventBus.send(writeHandlerId, Json.encode(logResult.body()));
                             }
                         })
                         .onFailure(err -> log.error(err.getMessage(), err.getCause()));
        });
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {
        final var periodicId = vertx.setPeriodic(DEFAULT_PERIODIC_TIME, id -> {
            final var localAsyncMap = vertx.sharedData()
                                           .getLocalAsyncMap(ApiConstants.SHARE_MAP_ID);
            localAsyncMap.compose(shareMap -> shareMap.get(ApiConstants.RUNNING_CONFIG_ID))
                         .compose(v -> {
                             if (!(v instanceof Boolean bool && bool)) {
                                 this.notifyObserver();
                                 return Future.succeededFuture();
                             } else {
                                 return localAsyncMap.compose(shareMap -> shareMap.remove(ApiConstants.RUNNING_CONFIG_ID));
                             }
                         })
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
        try {
            String sendId = (String) writeHandlerId;
            final var completableFuture = this.cache.get(recordLog.getDnsProviderType());
            return Future.fromCompletionStage(completableFuture, vertx.getOrCreateContext())
                         .compose(collection -> {
                             collection.add(recordLog);
                             return Future.succeededFuture(sendId);
                         });
        } catch (Exception e) {
            return Future.failedFuture(e.getMessage());
        }

    }
}
