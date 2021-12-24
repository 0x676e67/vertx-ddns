/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
