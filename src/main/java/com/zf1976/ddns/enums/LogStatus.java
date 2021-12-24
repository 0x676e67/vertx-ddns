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

package com.zf1976.ddns.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author mac
 * 2021/8/17 星期二 2:22 上午
 */
public enum LogStatus {

    /**
     * 无变化
     */
    RAW("无变化"),
    /**
     * err
     */
    ERROR("错误"),
    /**
     * 新增/创建
     */
    CREATE("新增"),
    /**
     * 更改
     */
    MODIFY("更新"),
    /**
     * 删除
     */
    DELETE("删除"),
    /**
     * 描述
     */
    DESCRIBE("描述"),
    /**
     * 新增失败
     */
    CREATE_FAIL("新增失败"),
    /**
     * 更新失败
     */
    MODIFY_FAIL("更新失败");

    private final String value;

    LogStatus(String value) {
        this.value = value;
    }


    @Override
    @JsonValue
    public String toString() {
        return this.value;
    }
}
