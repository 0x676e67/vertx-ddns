package com.zf1976.ddns.verticle.handler;

import com.zf1976.ddns.cache.AbstractMemoryLogCache;
import com.zf1976.ddns.cache.MemoryLogCache;
import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.pojo.DnsRecordLog;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author mac
 * 2021/8/26 星期四 9:03 下午
 */
public class LogCacheHandlerImpl implements LogCacheHandler<DnsProviderType, DnsRecordLog> {

    private final AbstractMemoryLogCache<DnsProviderType, DnsRecordLog> memoryLogCache;
    private final Vertx vertx;

    public LogCacheHandlerImpl(Vertx vertx) {
        this.memoryLogCache = new MemoryLogCache(10_000, 5, TimeUnit.HOURS);
        this.vertx = vertx;
    }

    @Override
    public Future<Void> clear(DnsProviderType dnsProviderType) {
        final var completableFuture = this.memoryLogCache.get(dnsProviderType);
        return Future.fromCompletionStage(completableFuture, vertx.getOrCreateContext())
                     .compose(v -> {
                         v.clear();
                         return Future.succeededFuture();
                     });
    }

    @Override
    public Future<Boolean> add(DnsProviderType dnsProviderType, DnsRecordLog dnsRecordLog) {
        return this.get(dnsProviderType)
                   .compose(v -> Future.succeededFuture(v.add(dnsRecordLog)));
    }

    @Override
    public Future<Collection<DnsRecordLog>> get(DnsProviderType dnsProviderType) {
        final var completableFuture = this.memoryLogCache.get(dnsProviderType);
        return Future.fromCompletionStage(completableFuture, vertx.getOrCreateContext());
    }

}
