package com.zf1976.ddns.pojo.vo;

/**
 * @author ant
 * Create by Ant on 2021/7/29 1:41 上午
 */
public class DnsRecordVo {

    String id;

    String domain;

    String rr;

    String value;

    public static DnsRecordVo.DnsRecordVoBuilder newBuilder() {
        return new DnsRecordVo.DnsRecordVoBuilder();
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
        return "DnsRecordVo{" +
                "id='" + id + '\'' +
                ", domain='" + domain + '\'' +
                ", rr='" + rr + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static final class DnsRecordVoBuilder {
        String id;
        String domain;
        String rr;
        String value;

        private DnsRecordVoBuilder() {
        }

        public DnsRecordVoBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DnsRecordVoBuilder withDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public DnsRecordVoBuilder withRr(String rr) {
            this.rr = rr;
            return this;
        }

        public DnsRecordVoBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public DnsRecordVo build() {
            DnsRecordVo dnsRecordVo = new DnsRecordVo();
            dnsRecordVo.rr = this.rr;
            dnsRecordVo.domain = this.domain;
            dnsRecordVo.value = this.value;
            dnsRecordVo.id = this.id;
            return dnsRecordVo;
        }
    }
}
