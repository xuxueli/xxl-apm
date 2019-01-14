package com.xxl.apm.client.message;

import com.xxl.rpc.util.IpUtil;

import java.io.Serializable;
import java.util.UUID;

/**
 * xxl-apm msg
 *
 * @author xuxueli 2018-12-29 16:53:25
 */
public abstract class XxlApmMsg implements Serializable {
    private static final long serialVersionUID = 42L;

    private String msgId = UUID.randomUUID().toString().replaceAll("-", "");
    private long addtime = System.currentTimeMillis();
    private String ip = IpUtil.getIp();
    private String hostname = IpUtil.getLocalAddress().getHostName();


    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }


    // tool
    public void complete(){
        // do something
    }

}
