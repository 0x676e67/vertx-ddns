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

package com.gngpp.ddns.enums;

/**
 * @author mac
 * 2021/7/8
 */
public enum DnsProviderType {

    /**
     * 阿里云 0000
     */
    ALIYUN((byte) 0),
    /**
     * 腾讯云 0001
     */
    DNSPOD((byte) (1)),
    /**
     * Cloudflare 0010
     */
    CLOUDFLARE((byte) (1 << 1)),
    /**
     * 华为云 0100
     */
    HUAWEI((byte) (1 << 2));

    public final byte index;

    DnsProviderType(byte index) {
        this.index = index;
    }

    public static DnsProviderType checkType(String value) {
        for (DnsProviderType type : values()) {
            if (type.toString()
                    .equals(value)) {
                return type;
            }
        }
        throw new RuntimeException("The DDNS api provider does not exist");
    }

    public boolean check(DnsProviderType dnsServiceType) {
        return this == dnsServiceType;
    }
}
