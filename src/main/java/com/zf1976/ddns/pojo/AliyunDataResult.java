package com.zf1976.ddns.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/15
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AliyunDataResult {

    @JsonProperty(value = "RequestId")
    private String requestId;

    @JsonProperty(value = "RecordId")
    private String recordId;

    private Object message;

    @JsonProperty(value = "TotalCount")
    private int totalCount;

    @JsonProperty(value = "PageNumber")
    private int pageNumber;

    @JsonProperty(value = "PageSize")
    private int pageSize;

    @JsonProperty(value = "DomainRecords")
    private DomainRecords domainRecords;

    public void setRequestId(String RequestId) {
        this.requestId = RequestId;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(int TotalCount) {
        this.totalCount = TotalCount;
    }

    public String getRecordId() {
        return recordId;
    }

    public Object getMessage() {
        return message;
    }

    public AliyunDataResult setMessage(Object message) {
        this.message = message;
        return this;
    }

    public AliyunDataResult setRecordId(String recordId) {
        this.recordId = recordId;
        return this;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public void setPageNumber(int PageNumber) {
        this.pageNumber = PageNumber;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int PageSize) {
        this.pageSize = PageSize;
    }

    public DomainRecords getDomainRecords() {
        return this.domainRecords;
    }

    public void setDomainRecords(DomainRecords DomainRecords) {
        this.domainRecords = DomainRecords;
    }

    @Override
    public String toString() {
        return "AliyunDataResult{" +
                "requestId='" + requestId + '\'' +
                ", recordId='" + recordId + '\'' +
                ", message=" + message +
                ", totalCount=" + totalCount +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", domainRecords=" + domainRecords +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DomainRecords {

        @JsonProperty(value = "Record")
        private List<Record> recordList;

        public List<Record> getRecordList() {
            return this.recordList;
        }

        public void setRecordList(List<Record> Record) {
            this.recordList = Record;
        }

        @Override
        public String toString() {
            return "domainRecords{" +
                    "recordList=" + recordList +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Record {

        @JsonProperty(value = "DomainName")
        private String domainName;

        @JsonProperty(value = "RecordId")
        private String recordId;

        @JsonProperty(value = "RR")
        private String rr;

        @JsonProperty(value = "Type")
        private String type;

        @JsonProperty(value = "Value")
        private String value;

        @JsonProperty(value = "Line")
        private String line;

        @JsonProperty(value = "Priority")
        private int priority;

        @JsonProperty(value = "TTL")
        private int ttl;

        @JsonProperty(value = "Status")
        private String status;

        @JsonProperty(value = "Locked")
        private boolean locked;

        public String getDomainName() {
            return this.domainName;
        }

        public void setDomainName(String DomainName) {
            this.domainName = DomainName;
        }

        public String getRecordId() {
            return this.recordId;
        }

        public void setRecordId(String RecordId) {
            this.recordId = RecordId;
        }

        public String getRr() {
            return this.rr;
        }

        public void setRr(String rr) {
            this.rr = rr;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String Type) {
            this.type = Type;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String Value) {
            this.value = Value;
        }

        public String getLine() {
            return this.line;
        }

        public void setLine(String Line) {
            this.line = Line;
        }

        public int getPriority() {
            return this.priority;
        }

        public void setPriority(int Priority) {
            this.priority = Priority;
        }

        public int getTtl() {
            return this.ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String Status) {
            this.status = Status;
        }

        public boolean getLocked() {
            return this.locked;
        }

        public void setLocked(boolean Locked) {
            this.locked = Locked;
        }

        @Override
        public String toString() {
            return "recordList{" +
                    "domainName='" + domainName + '\'' +
                    ", recordId='" + recordId + '\'' +
                    ", rr='" + rr + '\'' +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    ", line='" + line + '\'' +
                    ", priority=" + priority +
                    ", ttl=" + ttl +
                    ", status='" + status + '\'' +
                    ", locked=" + locked +
                    '}';
        }
    }
}
