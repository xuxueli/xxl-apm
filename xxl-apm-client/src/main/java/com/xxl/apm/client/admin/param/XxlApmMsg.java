package com.xxl.apm.client.admin.param;

import java.io.Serializable;

/**
 * xxl-apm msg
 *
 * @author xuxueli 2018-12-29 16:53:25
 */
public class XxlApmMsg implements Serializable {
    private static final long serialVersionUID = 42L;


    private long addtime;
    private String hostname;


    public long getAddtime() {
        return addtime;
    }

    public void setAddtime(long addtime) {
        this.addtime = addtime;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}
