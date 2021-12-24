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
