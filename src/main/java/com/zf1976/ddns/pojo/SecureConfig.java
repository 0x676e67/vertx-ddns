package com.zf1976.ddns.pojo;

import java.io.Serializable;

/**
 * Security Config
 * @author zf1976
 */
public class SecureConfig implements Serializable {

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
}
