package com.xxl.apm.client.message.impl;

import com.xxl.apm.client.message.XxlApmMsg;

import java.util.Map;

/**
 * metric msg, like "online_user、booking_count"
 *
 * @author xuxueli 2018-12-29
 */
public class XxlApmMetric extends XxlApmMsg {

    private String name;                    // like "online_user"
    private int count;
    private Map<String, String> param;      // like "platform、app_version"


    public XxlApmMetric() {
    }
    public XxlApmMetric(String name) {
        this.name = name;
        this.count = 1;
    }
    public XxlApmMetric(String name, int count, Map<String, String> param) {
        this.name = name;
        this.count = count;
        this.param = param;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }

}
