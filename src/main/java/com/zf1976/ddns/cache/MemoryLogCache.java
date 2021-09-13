package com.zf1976.ddns.cache;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zf1976.ddns.enums.DnsProviderType;
import com.zf1976.ddns.pojo.DnsRecordLog;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author mac
 * 2021/8/18 星期三 9:06 下午
 */
public class MemoryLogCache extends AbstractMemoryLogCache<DnsProviderType, DnsRecordLog> {

    private final AsyncCache<DnsProviderType, Collection<DnsRecordLog>> cache;

    public MemoryLogCache(long maximumSize) {
        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(5, TimeUnit.HOURS)
                             .maximumSize(maximumSize)
                             .buildAsync();
    }


    @Override
    public void store(DnsProviderType key, DnsRecordLog dnsRecordLog) {
        final var listCompletableFuture = cache.get(key, k -> this.createExpensiveLog(dnsRecordLog));
        if (listCompletableFuture != null) {
            cache.put(key, listCompletableFuture);
        }
    }

    @Override
    public Collection<CompletableFuture<Collection<DnsRecordLog>>> getAll() {
        return cache.asMap()
                    .values();
    }


    @Override
    public CompletableFuture<Collection<DnsRecordLog>> get(DnsProviderType key) {
        return cache.get(key, k -> this.createExpensiveLog());
    }

    @Override
    public void remove(DnsProviderType key) {
        cache.synchronous()
             .invalidate(key);
    }


}
