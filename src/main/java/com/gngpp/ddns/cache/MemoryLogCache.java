/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 gngpp
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

package com.gngpp.ddns.cache;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gngpp.ddns.pojo.DnsRecordLog;
import com.gngpp.ddns.enums.DnsProviderType;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author mac
 * 2021/8/18 星期三 9:06 下午
 */
public class MemoryLogCache extends AbstractMemoryLogCache<DnsProviderType, DnsRecordLog> {

    private final AsyncCache<DnsProviderType, Collection<DnsRecordLog>> cache;

    public MemoryLogCache(long maximumSize, int duration, TimeUnit unit) {
        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(duration, unit)
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
