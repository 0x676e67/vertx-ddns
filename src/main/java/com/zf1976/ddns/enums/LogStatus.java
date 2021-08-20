package com.zf1976.ddns.enums;

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
    public String toString() {
        return this.value;
    }
}
