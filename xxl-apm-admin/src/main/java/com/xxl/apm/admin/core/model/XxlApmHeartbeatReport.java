package com.xxl.apm.admin.core.model;

/**
 * @author xuxueli 2019-01-17
 */
public class XxlApmHeartbeatReport {

    private long id;

    private String appname;
    private long addtime;
    private String ip;
    private String hostname;

    private byte[] heartbeat_data;      // XxlApmHeartbeat data


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
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

    public byte[] getHeartbeat_data() {
        return heartbeat_data;
    }

    public void setHeartbeat_data(byte[] heartbeat_data) {
        this.heartbeat_data = heartbeat_data;
    }

}
