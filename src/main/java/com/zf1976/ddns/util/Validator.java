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

package com.zf1976.ddns.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 校验工具类
 *
 * @author mac
 * @date 2021/5/20
 */
@SuppressWarnings("UnusedReturnValue")
public class Validator<T> {

    /**
     * 初始化为真
     */
    private Predicate<T> predicate = t -> true;
    /**
     * 校验对象
     */
    private T value;

    private Validator() {
    }

    /**
     * 具有空检测初始化
     *
     * @param t   value
     * @param <T> 实际类型
     * @return Validator<T>
     */
    public static <T> Validator<T> of(T t) {
        return new Validator<T>().init(t);
    }

    public static <T> Validator<T> ofNullable(T t) {
        return new Validator<T>().nullInit(t);
    }

    public void isPresent(Consumer<? super T> action) {
        if (this.value != null) {
            action.accept(this.value);
        }
    }

    /**
     * 添加逻辑与一个校验策略
     *
     * @param predicate 校验策略
     * @return {@link Validator<T>}
     */
    public Validator<T> with(Predicate<T> predicate) {
        this.predicate = this.predicate.and(predicate);
        return this;
    }

    /**
     * 初始化校验
     *
     * @param t 类型
     * @return {@link Validator<T>}
     */
    private Validator<T> init(T t) {
        if (t == null) {
            throw new RuntimeException("data cannot been null!");
        }
        this.value = t;
        return this;
    }

    private Validator<T> nullInit(T t) {
        this.value = t;
        return this;
    }

    /**
     * 链式校验抛异常
     *
     * @param predicate         一个校验策略
     * @param exceptionSupplier 异常处理
     * @param <X>               如果没有值
     * @return Validator<T>
     * @throws X 异常
     */
    public <X extends Throwable> Validator<T> withValidated(Predicate<T> predicate,
                                                            Supplier<? extends X> exceptionSupplier) throws X {
        boolean validated = this.with(predicate)
                                .validated(this.value);
        if (!validated) {
            throw exceptionSupplier.get();
        }
        return this;
    }

    public  Validator<T> withValidated(Predicate<T> predicate, String message){
        return this.withValidated(predicate, () -> new RuntimeException(message));
    }

    /**
     * 添加一个逻辑或校验策略
     *
     * @param predicate 校验策略
     * @return {@link Validator<T>}
     */
    public Validator<T> or(Predicate<T> predicate) {
        this.predicate = this.predicate.or(predicate);
        return this;
    }


    /**
     * 执行校验
     *
     * @param t 对象
     * @return {@link boolean}
     */
    public boolean validated(T t) {
        if (t == null) {
            throw new RuntimeException("data cannot been null!");
        }
        return this.predicate.test(t);
    }

    public boolean Validated() {
        return this.validated(this.value);
    }

}
