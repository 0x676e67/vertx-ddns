package com.zf1976.ddns.cache;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author mac
 * 2021/8/20 星期五 7:31 上午
 */
public abstract class AbstractMemoryLogCache<T, L> {

    abstract public void store(T key, L dnsRecordLog);

    abstract public Collection<CompletableFuture<Collection<L>>> getAll();

    abstract public CompletableFuture<Collection<L>> get(T key);

    abstract public void remove(T key);

    protected Collection<L> createExpensiveLog(L dnsRecordLog) {
        final var copyOnWriteArrayList = new CopyOnWriteArrayList<L>();
        copyOnWriteArrayList.add(dnsRecordLog);
        return copyOnWriteArrayList;
    }

    protected Collection<L> createExpensiveLog() {
        return new CopyOnWriteArrayList<>();
    }
}
