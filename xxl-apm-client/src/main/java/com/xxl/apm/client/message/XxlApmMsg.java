package com.xxl.apm.client.message;

import com.xxl.rpc.util.IpUtil;

import java.io.Serializable;

/**
 * xxl-apm msg
 *
 * @author xuxueli 2018-12-29 16:53:25
 */
public abstract class XxlApmMsg implements Serializable {
    private static final long serialVersionUID = 42L;


    private long addtime = System.currentTimeMillis();
    private String ip = IpUtil.getIp();
    //private String hostname;


    public long getAddtime() {
        return addtime;
    }

    public void setAddtime(long addtime) {
        this.addtime = addtime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    // tool
    public void complete(){
        // do something
    }

}
