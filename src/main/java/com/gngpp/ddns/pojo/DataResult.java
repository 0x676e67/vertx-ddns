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

package com.gngpp.ddns.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gngpp.ddns.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;

/**
 * 请求响应对象
 *
 * @author ant
 * @since 2020/5/19
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@SuppressWarnings("rawtypes")
public class DataResult<T> implements Serializable {

    /**
     * 响应是否成功
     */
    private Boolean success;

    /**
     * 响应码
     */
    private Integer status;

    /**
     * 错误代码
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int errCode;

    /**
     * 错误消息
     */
    private String errMsg;

    /**
     * 正常消息
     */
    private String message;

    /**
     * 错误详情
     */
    private String errDetail;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 处理时间
     */
    private Long timestamp;

    /**
     * 路径
     */
    private String path;


    /**
     * 响应带数据的成功消息
     *
     * @param message 消息
     * @param <E>  E
     * @return 响应对象
     */
    public static <E> DataResult<E> success(String message) {
        DataResult<E> vo = new DataResult<>();
        vo.setMessage(message);
        vo.setSuccess(true);
        vo.setStatus(200);
        vo.setTimestamp(new Date().getTime());
        return vo;
    }

    /**
     * 响应带数据的成功消息
     *
     * @param data 数据
     * @param <E>  E
     * @return 响应对象
     */
    public static <E> DataResult<E> success(E data) {
        DataResult<E> vo = new DataResult<>();
        vo.setData(data);
        vo.setSuccess(true);
        vo.setStatus(200);
        vo.setTimestamp(new Date().getTime());
        return vo;
    }

    /**
     * 响应成功消息
     *
     * @param <E> E
     * @param sign param
     * @return 响应对象
     */
    public static <E> DataResult<E> success(@Nullable Void sign) {
        DataResult<E> vo = new DataResult<>();
        vo.setSuccess(true);
        vo.setStatus(200);
        vo.setTimestamp(new Date().getTime());
        return vo;
    }

    /**
     * 响应成功消息
     *
     * @param <E> E
     * @return 响应对象
     */
    public static <E> DataResult<E> success() {
        DataResult<E> vo = new DataResult<>();
        vo.setSuccess(true);
        vo.setStatus(200);
        vo.setTimestamp(new Date().getTime());
        return vo;
    }

    /**
     * 返回失败消息
     * @return 响应对象
     */
    public static <E> DataResult fail() {
        return fail((String) null);
    }

    /**
     * 返回失败消息
     *
     * @param errMsg 失败消息
     * @return 响应对象
     */
    public static <E> DataResult fail(String errMsg) {
        return fail(500, errMsg);
    }

    /**
     * 返回失败消息
     *
     * @param exception 异常对象
     * @return {@link DataResult}
     */
    public static <E> DataResult fail(Exception exception) {
        return fail(exception.getMessage());
    }

    /**
     * 返回失败消息
     *
     * @param errMsg  错误消息
     * @param errCode 错误码
     * @return 响应对象
     */
    public static <E> DataResult fail(int errCode, String errMsg) {
        DataResult<E> vo = new DataResult<>();
        vo.setSuccess(false);
        vo.setErrCode(errCode);
        vo.setErrMsg(errMsg);
        vo.setData(null);
        vo.setTimestamp(new Date().getTime());
        return vo;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrDetail() {
        return errDetail;
    }

    public void setErrDetail(String errDetail) {
        this.errDetail = errDetail;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "DataResult{" +
                "success=" + success +
                ", status=" + status +
                ", errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                ", message='" + message + '\'' +
                ", errDetail='" + errDetail + '\'' +
                ", data=" + data +
                ", timestamp='" + timestamp + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
