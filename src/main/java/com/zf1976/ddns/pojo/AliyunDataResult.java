package com.zf1976.ddns.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @author mac
 * @date 2021/7/15
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AliyunDataResult {

    private String requestId;

    private String recordId;

    private int TotalCount;

    private int PageNumber;

    private int PageSize;

    private DomainRecords DomainRecords;

    public void setRequestId(String RequestId){
        this.requestId = RequestId;
    }
    public String getRequestId(){
        return this.requestId;
    }
    public void setTotalCount(int TotalCount){
        this.TotalCount = TotalCount;
    }
    public int getTotalCount(){
        return this.TotalCount;
    }
    public String getRecordId() {
        return recordId;
    }

    public AliyunDataResult setRecordId(String recordId) {
        this.recordId = recordId;
        return this;
    }
    public void setPageNumber(int PageNumber){
        this.PageNumber = PageNumber;
    }
    public int getPageNumber(){
        return this.PageNumber;
    }
    public void setPageSize(int PageSize){
        this.PageSize = PageSize;
    }
    public int getPageSize(){
        return this.PageSize;
    }
    public void setDomainRecords(DomainRecords DomainRecords){
        this.DomainRecords = DomainRecords;
    }
    public DomainRecords getDomainRecords(){
        return this.DomainRecords;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DomainRecords {
        private List<Record> Record ;

        public void setRecord(List<Record> Record){
            this.Record = Record;
        }
        public List<Record> getRecord(){
            return this.Record;
        }

        @Override
        public String toString() {
            return "DomainRecords{" +
                    "Record=" + Record +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Record {
        private String DomainName;

        private String RecordId;

        private String RR;

        private String Type;

        private String Value;

        private String Line;

        private int Priority;

        private int TTL;

        private String Status;

        private boolean Locked;

        public void setDomainName(String DomainName){
            this.DomainName = DomainName;
        }
        public String getDomainName(){
            return this.DomainName;
        }
        public void setRecordId(String RecordId){
            this.RecordId = RecordId;
        }
        public String getRecordId(){
            return this.RecordId;
        }
        public void setRR(String RR){
            this.RR = RR;
        }
        public String getRR(){
            return this.RR;
        }
        public void setType(String Type){
            this.Type = Type;
        }
        public String getType(){
            return this.Type;
        }
        public void setValue(String Value){
            this.Value = Value;
        }
        public String getValue(){
            return this.Value;
        }
        public void setLine(String Line){
            this.Line = Line;
        }
        public String getLine(){
            return this.Line;
        }
        public void setPriority(int Priority){
            this.Priority = Priority;
        }
        public int getPriority(){
            return this.Priority;
        }
        public void setTTL(int TTL){
            this.TTL = TTL;
        }
        public int getTTL(){
            return this.TTL;
        }
        public void setStatus(String Status){
            this.Status = Status;
        }
        public String getStatus(){
            return this.Status;
        }
        public void setLocked(boolean Locked){
            this.Locked = Locked;
        }
        public boolean getLocked(){
            return this.Locked;
        }

        @Override
        public String toString() {
            return "Record{" +
                    "DomainName='" + DomainName + '\'' +
                    ", RecordId='" + RecordId + '\'' +
                    ", RR='" + RR + '\'' +
                    ", Type='" + Type + '\'' +
                    ", Value='" + Value + '\'' +
                    ", Line='" + Line + '\'' +
                    ", Priority=" + Priority +
                    ", TTL=" + TTL +
                    ", Status='" + Status + '\'' +
                    ", Locked=" + Locked +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AliyunDataResult{" +
                "requestId='" + requestId + '\'' +
                ", recordId='" + recordId + '\'' +
                ", TotalCount=" + TotalCount +
                ", PageNumber=" + PageNumber +
                ", PageSize=" + PageSize +
                ", DomainRecords=" + DomainRecords +
                '}';
    }
}
