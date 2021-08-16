package com.zf1976.ddns.pojo;

import com.zf1976.ddns.enums.DnsProviderType;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ant
 * Create by Ant on 2021/8/17 1:52 AM
 */
public class Log implements Serializable {

    private DnsProviderType dnsProviderType;

    private String content;

    private Date date;


}
