package com.xxl.apm.client;

import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.registry.client.util.json.BasicJson;

/**
 * @author xuxueli 2018-12-22 18:31:48
 */
public class XxlApm {


    // env param
    private static String appname;
    public static final ThreadLocal<String> parentMsgId = new ThreadLocal<String>();


    /**
     * init
     */
    public static void init(String appname){
        // valid appname
        if (appname==null || appname.trim().length()==0) {
            throw new RuntimeException("xxl-apm, appname cannot be empty.");
        }
        XxlApm.appname = appname;
    }

    /**
     * appname
     */
    public static String getAppname() {
        return appname;
    }

    /**
     * submit msg
     *
     * @param apmMsg
     * @return
     */
    public static boolean submit(XxlApmMsg apmMsg){
        // complete message
        apmMsg.complete();

        // todo: send message, fail retry > file + retry tread
        System.out.println(BasicJson.toJson(apmMsg));;

        return true;
    }


}
