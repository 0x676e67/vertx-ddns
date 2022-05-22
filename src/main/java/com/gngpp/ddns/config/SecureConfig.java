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

package com.gngpp.ddns.config;

import com.gngpp.ddns.annotation.ConfigPrefix;

import java.io.Serializable;

/**
 * Security Config
 * @author gngpp
 */
@ConfigPrefix(value = "defaultSecureConfig")
public class SecureConfig implements Serializable , Cloneable{

    /**
     * not allow wan access
     */
    private Boolean notAllowWanAccess = Boolean.TRUE;

    /**
     * login username
     */
    private String username;

    /**
     * login password
     */
    private String password;

    public SecureConfig() {
    }

    public SecureConfig(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public SecureConfig(Boolean notAllowWanAccess, String username, String password) {
        this.notAllowWanAccess = notAllowWanAccess;
        this.username = username;
        this.password = password;
    }

    public Boolean getNotAllowWanAccess() {
        return notAllowWanAccess;
    }

    public void setNotAllowWanAccess(Boolean notAllowWanAccess) {
        this.notAllowWanAccess = notAllowWanAccess;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Secure{" +
                "notAllowWanAccess=" + notAllowWanAccess +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SecureConfig secureConfig = null;
        try{
            secureConfig = (SecureConfig)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return secureConfig;
    }
}
