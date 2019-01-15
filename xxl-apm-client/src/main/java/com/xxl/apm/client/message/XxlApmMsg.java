package com.xxl.apm.client.message;

import com.xxl.apm.client.XxlApm;
import com.xxl.rpc.util.IpUtil;

import java.io.Serializable;

/**
 * xxl-apm msg
 *
 * @author xuxueli 2018-12-29 16:53:25
 */
public abstract class XxlApmMsg implements Serializable {
    private static final long serialVersionUID = 42L;

    private String parentMsgId;
    private String msgId;

    private long addtime;
    private String ip;
    private String hostname;


    public String getParentMsgId() {
        return parentMsgId;
    }

    public void setParentMsgId(String parentMsgId) {
        this.parentMsgId = parentMsgId;
    }

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
        this.parentMsgId = XxlApm.getParentMsgId();
        this.msgId = XxlApm.generateMsgId();

        this.addtime = System.currentTimeMillis();
        this.ip = IpUtil.getIp();
        this.hostname = IpUtil.getLocalAddress().getHostName();
    }

}
