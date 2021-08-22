package com.zf1976.ddns.config;

import com.zf1976.ddns.annotation.ConfigPrefix;

import java.io.Serializable;

/**
 * Security Config
 * @author zf1976
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
