package com.zf1976.ddns.util;

import com.zf1976.ddns.annotation.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * @author mac
 * @date 2021/7/7
 */
public class CollectionUtil {
    public CollectionUtil() {
    }

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
