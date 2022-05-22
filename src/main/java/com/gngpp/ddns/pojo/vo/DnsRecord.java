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

package com.gngpp.ddns.pojo.vo;

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
