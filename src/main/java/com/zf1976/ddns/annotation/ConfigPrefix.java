package com.zf1976.ddns.annotation;

import java.lang.annotation.*;

/**
 * @author mac
 * 2021/7/6
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigPrefix {
    /**
     * json属性前缀值
     */
    String value();
}
