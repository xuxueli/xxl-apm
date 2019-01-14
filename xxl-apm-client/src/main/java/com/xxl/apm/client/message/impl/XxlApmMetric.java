package com.xxl.apm.client.message.impl;

import com.xxl.apm.client.message.XxlApmMsg;

import java.util.Map;

/**
 * metric msg, like "online use、booking count"
 *
 * @author xuxueli 2018-12-29
 */
public class XxlApmMetric extends XxlApmMsg {

    private String name;                // like "online_user"
    private int count;
    private Map<String, String> param;  // like "ip=xxx"



}
