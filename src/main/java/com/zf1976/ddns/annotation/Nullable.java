package com.zf1976.ddns.annotation;

import java.lang.annotation.*;

/**
 * @author mac
 * @date 2021/7/7
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Nullable {
}
