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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author mac
 * 2021/7/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DnspodDataResult {

    @JsonProperty("Response")
    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "DnspodDataResult{" +
                "response=" + response +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordCountInfo {

        @JsonProperty("SubdomainCount")
        private int subDomainCount;
        @JsonProperty("TotalCount")
        private int totalCount;
        @JsonProperty("ListCount")
        private int listCount;

        public int getSubDomainCount() {
            return subDomainCount;
        }

        public void setSubDomainCount(int subDomainCount) {
            this.subDomainCount = subDomainCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getListCount() {
            return listCount;
        }

        public void setListCount(int listCount) {
            this.listCount = listCount;
        }

        @Override
        public String toString() {
            return "RecordCountInfo{" +
                    "subDomainCount=" + subDomainCount +
                    ", totalCount=" + totalCount +
                    ", listCount=" + listCount +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordList {

        @JsonProperty("RecordId")
        private int recordId;
        @JsonProperty("Value")
        private String value;
        @JsonProperty("Status")
        private String status;
        @JsonProperty("UpdatedOn")
        private String updatedOn;
        @JsonProperty("Name")
        private String name;
        @JsonProperty("Line")
        private String line;
        @JsonProperty("LineId")
        private String lineId;
        @JsonProperty("Type")
        private String type;
        @JsonProperty("Weight")
        private String weight;
        @JsonProperty("MonitorStatus")
        private String monitorStatus;
        @JsonProperty("Remark")
        private String remark;
        @JsonProperty("TTL")
        private int ttl;
        @JsonProperty("MX")
        private int mx;

        public int getRecordId() {
            return recordId;
        }

        public void setRecordId(int recordId) {
            this.recordId = recordId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getUpdatedOn() {
            return updatedOn;
        }

        public void setUpdatedOn(String updatedOn) {
            this.updatedOn = updatedOn;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public String getLineId() {
            return lineId;
        }

        public void setLineId(String lineId) {
            this.lineId = lineId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getMonitorStatus() {
            return monitorStatus;
        }

        public void setMonitorStatus(String monitorStatus) {
            this.monitorStatus = monitorStatus;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public int getMx() {
            return mx;
        }

        public void setMx(int mx) {
            this.mx = mx;
        }

        @Override
        public String toString() {
            return "RecordList{" +
                    "recordId=" + recordId +
                    ", value='" + value + '\'' +
                    ", status='" + status + '\'' +
                    ", updatedOn='" + updatedOn + '\'' +
                    ", name='" + name + '\'' +
                    ", line='" + line + '\'' +
                    ", lineId='" + lineId + '\'' +
                    ", type='" + type + '\'' +
                    ", weight='" + weight + '\'' +
                    ", monitorStatus='" + monitorStatus + '\'' +
                    ", remark='" + remark + '\'' +
                    ", ttl=" + ttl +
                    ", mx=" + mx +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {
        @JsonProperty(value = "Code")
        private String code;

        @JsonProperty(value = "Message")
        private String message;

        public String getCode() {
            return code;
        }

        public Error setCode(String code) {
            this.code = code;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public Error setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "code='" + code + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        @JsonProperty("RequestId")
        private String requestId;
        @JsonProperty("RecordId")
        private String recordId;
        @JsonProperty("RecordCountInfo")
        private RecordCountInfo recordCountInfo;
        @JsonProperty("RecordList")
        private List<RecordList> recordList;
        @JsonProperty("Error")
        private Error error;

        public Error getError() {
            return error;
        }

        public Response setError(Error error) {
            this.error = error;
            return this;
        }

        public String getRecordId() {
            return recordId;
        }

        public Response setRecordId(String recordId) {
            this.recordId = recordId;
            return this;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public RecordCountInfo getRecordCountInfo() {
            return recordCountInfo;
        }

        public void setRecordCountInfo(RecordCountInfo recordCountInfo) {
            this.recordCountInfo = recordCountInfo;
        }

        public List<RecordList> getRecordList() {
            return recordList;
        }

        public void setRecordList(List<RecordList> recordList) {
            this.recordList = recordList;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "requestId='" + requestId + '\'' +
                    ", recordId='" + recordId + '\'' +
                    ", recordCountInfo=" + recordCountInfo +
                    ", recordList=" + recordList +
                    ", error=" + error +
                    '}';
        }
    }
}

