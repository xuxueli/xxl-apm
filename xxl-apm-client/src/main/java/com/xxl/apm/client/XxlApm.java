package com.xxl.apm.client;

import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.registry.client.util.json.BasicJson;

/**
 * @author xuxueli 2018-12-22 18:31:48
 */
public class XxlApm {


    public static boolean submit(XxlApmMsg apmMsg){
        // complete message
        apmMsg.complete();

        // todo: send message, fail retry > file + retry tread
        System.out.println(BasicJson.toJson(apmMsg));;

        return true;
    }



}
