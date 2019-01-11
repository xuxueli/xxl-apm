package com.xxl.apm.client;

import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmTransaction;

import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2018-12-22 18:31:48
 */
public class XxlApm {


    public static boolean submit(XxlApmMsg apmMsg){
        // complete message
        apmMsg.complete();
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        XxlApm.submit(new XxlApmEvent("URL", "/user/add"));


        XxlApmTransaction transaction = new XxlApmTransaction("URL", "/user/add");
        TimeUnit.SECONDS.sleep(2);
        transaction.setStatus("fail");


        XxlApm.submit(transaction);
    }

}
