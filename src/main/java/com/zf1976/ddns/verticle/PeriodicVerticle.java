package com.zf1976.ddns.verticle;

import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.enums.LogStatus;
import com.zf1976.ddns.pojo.DnsRecordLog;
import com.zf1976.ddns.util.CollectionUtil;
import com.zf1976.ddns.verticle.codec.DnsRecordLogMessageCodec;
import com.zf1976.ddns.verticle.handler.LogCacheHandler;
import com.zf1976.ddns.verticle.handler.webhook.CompositeWebhookHandler;
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
    private final LogCacheHandler<DnsProviderType, DnsRecordLog> consumerHandler;
    private final CompositeWebhookHandler compositeWebhookHandler;

    public PeriodicVerticle(DnsRecordObserver observer,
                            LogCacheHandler<DnsProviderType, DnsRecordLog> logCacheHandler,
                            CompositeWebhookHandler compositeWebhookHandler) {
        this.addObserver(observer);
        this.consumerHandler = logCacheHandler;
        this.compositeWebhookHandler = compositeWebhookHandler;
    }

    public PeriodicVerticle(List<DnsRecordObserver> observers,
                            LogCacheHandler<DnsProviderType, DnsRecordLog> logCacheHandler,
                            CompositeWebhookHandler compositeWebhookHandler) {
        this.consumerHandler = logCacheHandler;
        this.compositeWebhookHandler =compositeWebhookHandler;
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
        // local async map
        final var localAsyncMap = vertx.sharedData()
                                       .getLocalAsyncMap(ApiConstants.SHARE_MAP_ID);
        // send dns record resolve log
        eventBus.consumer(ApiConstants.CONFIG_SUBJECT_ADDRESS, logResult -> {
            DnsRecordLog recordLog = (DnsRecordLog) logResult.body();
            localAsyncMap.compose(shareMap -> shareMap.get(ApiConstants.SOCKJS_WRITE_HANDLER_ID))
                         .compose(writeHandlerId -> this.storeMemoryLog(writeHandlerId, recordLog))
                         .compose(writeHandlerId -> localAsyncMap.compose(shareMap -> shareMap.get(ApiConstants.SOCKJS_SELECT_PROVIDER_TYPE))
                                                                 .compose(v -> this.checkDnsProviderType(writeHandlerId, v, recordLog)))
                         .onSuccess(writeHandlerId -> {
                             if (writeHandlerId != null) {
                                 eventBus.send(writeHandlerId, Json.encode(recordLog));
                             }
                             if (!(recordLog.getLogStatus() == LogStatus.RAW)) {
                                 this.compositeWebhookHandler.send(recordLog)
                                                             .onSuccess(compositeFuture -> {
                                                                 compositeFuture.onComplete(event -> {
                                                                     if (event.failed()) {
                                                                         log.error(event.cause());
                                                                         eventBus.send(writeHandlerId, Json.encode(event.cause().getMessage()));
                                                                     }
                                                                 });
                                                             });
                             }
                         })
                         .onFailure(err -> log.error(err.getMessage(), err.getCause()));
        });
        super.start(startPromise);
    }

    @Override
    public void start() throws Exception {
        final var localAsyncMap = vertx.sharedData().getLocalAsyncMap(ApiConstants.SHARE_MAP_ID);
        final var periodicId = vertx.setPeriodic(DEFAULT_PERIODIC_TIME, id -> localAsyncMap.compose(shareMap -> shareMap.get(ApiConstants.RUNNING_CONFIG_ID))
                                                                                           .compose(v -> {
                                                                                               if (!(v instanceof Boolean bool && bool)) {
                                                                                                   this.notifyObserver();
                                                                                                   return Future.succeededFuture();
                                                                                               } else {
                                                                                                   return localAsyncMap.compose(shareMap -> shareMap.remove(ApiConstants.RUNNING_CONFIG_ID));
                                                                                               }
                                                                                           })
                                                                                           .onFailure(err -> log.error(err.getMessage(), err.getCause())));
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
            return this.consumerHandler.add(recordLog.getDnsProviderType(), recordLog)
                                       .compose(v -> Future.succeededFuture(sendId));
        } catch (Exception e) {
            return Future.failedFuture(e.getMessage());
        }
    }

    private Future<String> checkDnsProviderType(String writeHandlerId, Object v, DnsRecordLog dnsRecordLog) {
        try {
            final var dnsProviderType = (DnsProviderType) v;
            if (dnsRecordLog.getDnsProviderType()
                         .check(dnsProviderType)) {
                return Future.succeededFuture(writeHandlerId);
            }
            return Future.succeededFuture();
        } catch (Exception e) {
            return Future.failedFuture(e.getMessage());
        }
    }

}
