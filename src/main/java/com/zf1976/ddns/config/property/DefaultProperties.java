/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
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

package com.zf1976.ddns.config.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zf1976.ddns.annotation.ConfigPrefix;

import java.util.List;

/**
 * @author mac
 * 2021/7/7
 */
@ConfigPrefix(value = "default")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultProperties {

    /**
     * 默认用户名
     */
    private String defaultUsername;

    /**
     * 默认密码
     */
    private String defaultPassword;

    /**
     * ip Api
     */
    private List<String> ipApiList;

    /**
     * DNS Server List
     */
    private List<String> dnsServerList;

    public List<String> getIpApiList() {
        return ipApiList;
    }

    public DefaultProperties setIpApiList(List<String> ipApiList) {
        this.ipApiList = ipApiList;
        return this;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public DefaultProperties setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
        return this;
    }

    public DefaultProperties setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
        return this;
    }

    public List<String> getDnsServerList() {
        return dnsServerList;
    }

    public DefaultProperties setDnsServerList(List<String> dnsServerList) {
        this.dnsServerList = dnsServerList;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultProperties{" +
                "ipApiList=" + ipApiList +
                ", dnsServerList=" + dnsServerList +
                '}';
    }
}
