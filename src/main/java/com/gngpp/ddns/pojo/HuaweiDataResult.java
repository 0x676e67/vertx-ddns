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

import java.util.Date;
import java.util.List;

/**
 * @author mac
 * @date 2021/7/24
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class HuaweiDataResult {

    private List<Recordsets> recordsets;
    private List<HuaweiDataResult.Zones> zones;
    private HuaweiDataResult.Links links;
    private HuaweiDataResult.Metadata metadata;
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public HuaweiDataResult setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public HuaweiDataResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public List<Zones> getZones() {
        return zones;
    }

    public void setZones(List<HuaweiDataResult.Zones> zones) {
        this.zones = zones;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Recordsets> getRecordsets() {
        return recordsets;
    }

    public HuaweiDataResult setRecordsets(List<Recordsets> recordsets) {
        this.recordsets = recordsets;
        return this;
    }

    @Override
    public String toString() {
        return "HuaweiDataResult{" +
                "recordsets=" + recordsets +
                ", zones=" + zones +
                ", links=" + links +
                ", metadata=" + metadata +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @SuppressWarnings({"unused", "SpellCheckingInspection"})
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recordsets {

        private String id;
        private String name;
        private String description;
        private String type;
        private int ttl;
        private List<String> records;
        private String status;
        @JsonProperty("zone_id")
        private String zoneId;
        @JsonProperty("zone_name")
        private String zoneName;
        @JsonProperty("create_at")
        private Date createAt;
        @JsonProperty("update_at")
        private Date updateAt;
        @JsonProperty("default")
        private boolean ddefault;
        @JsonProperty("project_id")
        private String projectId;
        private Links links;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public List<String> getRecords() {
            return records;
        }

        public void setRecords(List<String> records) {
            this.records = records;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }

        public String getZoneName() {
            return zoneName;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        public Date getCreateAt() {
            return createAt;
        }

        public void setCreateAt(Date createAt) {
            this.createAt = createAt;
        }

        public Date getUpdateAt() {
            return updateAt;
        }

        public void setUpdateAt(Date updateAt) {
            this.updateAt = updateAt;
        }

        public void setDDefault(boolean ddefault) {
            this.ddefault = ddefault;
        }

        public boolean getDefault() {
            return ddefault;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public Links getLinks() {
            return links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }

        @Override
        public String toString() {
            return "Recordsets{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", type='" + type + '\'' +
                    ", ttl=" + ttl +
                    ", records=" + records +
                    ", status='" + status + '\'' +
                    ", zoneId='" + zoneId + '\'' +
                    ", zoneName='" + zoneName + '\'' +
                    ", createAt=" + createAt +
                    ", updateAt=" + updateAt +
                    ", ddefault=" + ddefault +
                    ", projectId='" + projectId + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {

        private String self;

        public String getSelf() {
            return self;
        }

        public void setSelf(String self) {
            this.self = self;
        }

        @Override
        public String toString() {
            return "Links{" +
                    "self='" + self + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {

        @JsonProperty("total_count")
        private int totalCount;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        @Override
        public String toString() {
            return "Metadata{" +
                    "totalCount=" + totalCount +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Zones {

        private String id;
        private String name;
        private String description;
        private String email;
        private int ttl;
        private int serial;
        private List<String> masters;
        private String status;
        @JsonProperty("pool_id")
        private String poolId;
        @JsonProperty("project_id")
        private String projectId;
        @JsonProperty("zone_type")
        private String zoneType;
        @JsonProperty("created_at")
        private Date createdAt;
        @JsonProperty("updated_at")
        private Date updatedAt;
        @JsonProperty("record_num")
        private int recordNum;
        private Links links;
        @JsonProperty("enterprise_project_id")
        private String enterpriseProjectId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public int getSerial() {
            return serial;
        }

        public void setSerial(int serial) {
            this.serial = serial;
        }

        public List<String> getMasters() {
            return masters;
        }

        public void setMasters(List<String> masters) {
            this.masters = masters;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPoolId() {
            return poolId;
        }

        public void setPoolId(String poolId) {
            this.poolId = poolId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getZoneType() {
            return zoneType;
        }

        public void setZoneType(String zoneType) {
            this.zoneType = zoneType;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }

        public int getRecordNum() {
            return recordNum;
        }

        public void setRecordNum(int recordNum) {
            this.recordNum = recordNum;
        }

        public Links getLinks() {
            return links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }

        public String getEnterpriseProjectId() {
            return enterpriseProjectId;
        }

        public void setEnterpriseProjectId(String enterpriseProjectId) {
            this.enterpriseProjectId = enterpriseProjectId;
        }

        @Override
        public String toString() {
            return "Zones{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", email='" + email + '\'' +
                    ", ttl=" + ttl +
                    ", serial=" + serial +
                    ", masters=" + masters +
                    ", status='" + status + '\'' +
                    ", poolId='" + poolId + '\'' +
                    ", projectId='" + projectId + '\'' +
                    ", zoneType='" + zoneType + '\'' +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    ", recordNum=" + recordNum +
                    ", links=" + links +
                    ", enterpriseProjectId='" + enterpriseProjectId + '\'' +
                    '}';
        }
    }
}
