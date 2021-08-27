package com.zf1976.ddns.verticle.handler;

import io.vertx.core.Future;

import java.util.Collection;

/**
 * @author mac
 * 2021/8/21 星期六 6:35 下午
 */
public interface LogCacheHandler<K, V> {

    Future<Boolean> add(K k, V v);

    Future<Collection<V>> get(K k);

    Future<Void> clear(K k);

}
