package com.zf1976.ddns.pojo.vo;

/**
 * @author ant
 * Create by Ant on 2021/7/29 1:41 上午
 */
public class DnsRecord {

    String id;

    String domain;

    String rr;

    String value;

    public static DnsRecordBuilder newBuilder() {
        return new DnsRecordBuilder();
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getRr() {
        return rr;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DnsRecord{" +
                "id='" + id + '\'' +
                ", domain='" + domain + '\'' +
                ", rr='" + rr + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static final class DnsRecordBuilder {
        String id;
        String domain;
        String rr;
        String value;

        private DnsRecordBuilder() {
        }

        public DnsRecordBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DnsRecordBuilder withDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public DnsRecordBuilder withRr(String rr) {
            this.rr = rr;
            return this;
        }

        public DnsRecordBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public DnsRecord build() {
            DnsRecord dnsRecord = new DnsRecord();
            dnsRecord.rr = this.rr;
            dnsRecord.domain = this.domain;
            dnsRecord.value = this.value;
            dnsRecord.id = this.id;
            return dnsRecord;
        }
    }
}
