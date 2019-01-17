package com.xxl.apm.admin.core.model;

import java.util.Date;

/**
 * @author xuxueli 2019-01-17
 */
public class XxlApmHeartbeatReport {

    private long id;

    private String ip;
    private String hostname;
    private Date period;                // accurate to min, 'yyyy-MM-dd HH-mm'

    private byte[] heartbeat_data;      // XxlApmHeartbeat data

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Date getPeriod() {
        return period;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    public byte[] getHeartbeat_data() {
        return heartbeat_data;
    }

    public void setHeartbeat_data(byte[] heartbeat_data) {
        this.heartbeat_data = heartbeat_data;
    }

}
