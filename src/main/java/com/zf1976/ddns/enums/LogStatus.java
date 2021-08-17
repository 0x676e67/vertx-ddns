package com.zf1976.ddns.enums;

/**
 * @author mac
 * 2021/8/17 星期二 2:22 上午
 */
public enum LogStatus {

    /**
     * 无变化
     */
    ROW,
    /**
     * 新增/创建
     */
    CREATE,
    /**
     * 更改
     */
    MODIFY,
    /**
     * 删除
     */
    DELETE,
    /**
     * 描述
     */
    DESCRIBE,
    /**
     * 新增失败
     */
    CREATE_FAIL,
    /**
     * 更新失败
     */
    MODIFY_FAIL

}
