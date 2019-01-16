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
    private long count;
    private Map<String, String> param;      // like "platform=app、app_version=1.2"


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

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }


    // tool
    @Override
    public void complete() {
        super.complete();
    }

}
