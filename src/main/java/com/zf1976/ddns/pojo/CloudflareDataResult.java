package com.zf1976.ddns.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudflareDataResult<T> {

    private T result;
    private boolean success;
    private List<Object> errors;
    private List<Object> messages;
    @JsonProperty("result_info")
    private ResultInfo resultInfo;

    public T getResult() {
        return result;
    }

    public CloudflareDataResult<T> setResult(T result) {
        this.result = result;
        return this;
    }

    public void setSuccess(boolean success) {
         this.success = success;
     }
     public boolean getSuccess() {
         return success;
     }

    public void setErrors(List<Object> errors) {
         this.errors = errors;
     }
     public List<Object> getErrors() {
         return errors;
     }

    public void setMessages(List<Object> messages) {
         this.messages = messages;
     }
     public List<Object> getMessages() {
         return messages;
     }

    public void setResultInfo(ResultInfo resultInfo) {
         this.resultInfo = resultInfo;
     }
     public ResultInfo getResultInfo() {
         return resultInfo;
     }

    @Override
    public String toString() {
        return "CloudflareDataResult{" +
                "result=" + result +
                ", success=" + success +
                ", errors=" + errors +
                ", messages=" + messages +
                ", resultInfo=" + resultInfo +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {

        @JsonProperty("auto_added")
        private boolean autoAdded;
        @JsonProperty("managed_by_apps")
        private boolean managedByApps;
        @JsonProperty("managed_by_argo_tunnel")
        private boolean managedByArgoTunnel;
        private String source;
        public void setAutoAdded(boolean autoAdded) {
            this.autoAdded = autoAdded;
        }
        public boolean getAutoAdded() {
            return autoAdded;
        }

        public void setManagedByApps(boolean managedByApps) {
            this.managedByApps = managedByApps;
        }
        public boolean getManagedByApps() {
            return managedByApps;
        }

        public void setManagedByArgoTunnel(boolean managedByArgoTunnel) {
            this.managedByArgoTunnel = managedByArgoTunnel;
        }
        public boolean getManagedByArgoTunnel() {
            return managedByArgoTunnel;
        }

        public void setSource(String source) {
            this.source = source;
        }
        public String getSource() {
            return source;
        }

        @Override
        public String toString() {
            return "Meta{" +
                    "autoAdded=" + autoAdded +
                    ", managedByApps=" + managedByApps +
                    ", managedByArgoTunnel=" + managedByArgoTunnel +
                    ", source='" + source + '\'' +
                    '}';
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        private String id;
        @JsonProperty("zone_id")
        private String zoneId;
        @JsonProperty("zone_name")
        private String zoneName;
        private String name;
        private String type;
        private String content;
        private boolean proxiable;
        private boolean proxied;
        private int ttl;
        private boolean locked;
        private Meta meta;
        @JsonProperty("created_on")
        private Date createdOn;
        @JsonProperty("modified_on")
        private Date modifiedOn;
        public void setId(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }
        public String getZoneId() {
            return zoneId;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }
        public String getZoneName() {
            return zoneName;
        }

        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

        public void setType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }

        public void setContent(String content) {
            this.content = content;
        }
        public String getContent() {
            return content;
        }

        public void setProxiable(boolean proxiable) {
            this.proxiable = proxiable;
        }
        public boolean getProxiable() {
            return proxiable;
        }

        public void setProxied(boolean proxied) {
            this.proxied = proxied;
        }
        public boolean getProxied() {
            return proxied;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }
        public int getTtl() {
            return ttl;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }
        public boolean getLocked() {
            return locked;
        }

        public void setMeta(Meta meta) {
            this.meta = meta;
        }
        public Meta getMeta() {
            return meta;
        }

        public void setCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
        }
        public Date getCreatedOn() {
            return createdOn;
        }

        public void setModifiedOn(Date modifiedOn) {
            this.modifiedOn = modifiedOn;
        }
        public Date getModifiedOn() {
            return modifiedOn;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "id='" + id + '\'' +
                    ", zoneId='" + zoneId + '\'' +
                    ", zoneName='" + zoneName + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", content='" + content + '\'' +
                    ", proxiable=" + proxiable +
                    ", proxied=" + proxied +
                    ", ttl=" + ttl +
                    ", locked=" + locked +
                    ", meta=" + meta +
                    ", createdOn=" + createdOn +
                    ", modifiedOn=" + modifiedOn +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultInfo {

        private int page;
        @JsonProperty("per_page")
        private int perPage;
        private int count;
        @JsonProperty("total_count")
        private int totalCount;
        @JsonProperty("total_pages")
        private int totalPages;
        public void setPage(int page) {
            this.page = page;
        }
        public int getPage() {
            return page;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }
        public int getPerPage() {
            return perPage;
        }

        public void setCount(int count) {
            this.count = count;
        }
        public int getCount() {
            return count;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        public int getTotalPages() {
            return totalPages;
        }

        @Override
        public String toString() {
            return "ResultInfo{" +
                    "page=" + page +
                    ", perPage=" + perPage +
                    ", count=" + count +
                    ", totalCount=" + totalCount +
                    ", totalPages=" + totalPages +
                    '}';
        }
    }

}
