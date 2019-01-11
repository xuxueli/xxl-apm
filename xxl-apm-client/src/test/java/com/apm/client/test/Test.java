package com.apm.client.test;

import com.xxl.apm.client.XxlApm;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import com.xxl.apm.client.message.impl.XxlApmTransaction;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2019-01-11
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {

        // event message
        XxlApm.submit(new XxlApmEvent("URL", "/user/add"));

        // transaction
        XxlApmTransaction transaction = new XxlApmTransaction("URL", "/user/add");
        TimeUnit.SECONDS.sleep(1);
        transaction.setStatus("fail");
        transaction.setParam(new HashMap<String, String>(){{
            put("userid", "1001");
        }});
        XxlApm.submit(transaction);

        // heartbeat
        XxlApm.submit(new XxlApmHeartbeat());


    }

}
