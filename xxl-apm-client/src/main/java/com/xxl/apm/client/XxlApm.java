package com.xxl.apm.client;

import com.xxl.apm.client.factory.XxlApmFactory;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.registry.client.util.json.BasicJson;

/**
 * @author xuxueli 2018-12-22 18:31:48
 */
public class XxlApm {


    private XxlApmFactory xxlApmFactory;

    public XxlApm(XxlApmFactory xxlApmFactory) {
        this.xxlApmFactory = xxlApmFactory;
    }


    // ---------------------- tool ----------------------

    private static XxlApm instance = null;

    /**
     * set instance of XxlApm
     *
     * @param xxlApmFactory
     */
    public synchronized static void setInstance(XxlApmFactory xxlApmFactory) {
        if (XxlApm.instance != null) {
            throw new RuntimeException("xxl-apm, repeat generate XxlApm.");
        }
        XxlApm.instance = new XxlApm(xxlApmFactory);
    }


    /**
     * remove ParentMsgId
     */
    public static void removeParentMsgId(){
        instance.xxlApmFactory.parentMsgId.remove();
    }

    /**
     * set ParentMsgId
     *
     * @param value
     */
    public static void setParentMsgId(String value){
        instance.xxlApmFactory.parentMsgId.set(value);
    }

    /**
     * get ParentMsgId
     *
     * @return
     */
    public static String getParentMsgId(){
        return instance.xxlApmFactory.parentMsgId.get();
    }


    /**
     * generate MsgId
     *
     * @return
     */
    public static String generateMsgId(){
        return instance.xxlApmFactory.generateMsgId();
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

        /**
         * todo: send message
         *
         *  - async queue send; 200 msg per invoke
         *  - queue-max or fail, write file
         *  - fail-retry thread, read file
         */
        System.out.println(BasicJson.toJson(apmMsg));;

        return true;
    }


}
