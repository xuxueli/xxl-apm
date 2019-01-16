package com.apm.client.test;

import com.xxl.apm.client.XxlApm;
import com.xxl.apm.client.factory.XxlApmFactory;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import com.xxl.apm.client.message.impl.XxlApmMetric;
import com.xxl.apm.client.message.impl.XxlApmTransaction;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2019-01-11
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {

        // start
        XxlApmFactory xxlApmFactory = new XxlApmFactory();
        xxlApmFactory.setAppname("demo-project");
        xxlApmFactory.setAdminAddress("http://localhost:8080/xxl-apm-admin");
        xxlApmFactory.setAccessToken(null);
        xxlApmFactory.setMsglogpath("/data/applogs/xxl-apm/msglogpath");
        xxlApmFactory.start();
        TimeUnit.SECONDS.sleep(1);

        // event message
        XxlApm.report(new XxlApmEvent("URL", "/user/add"));

        // transaction
        XxlApmTransaction transaction = new XxlApmTransaction("URL", "/user/query");
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(1);
        }
        transaction.setStatus("fail");
        transaction.setParam(new HashMap<String, String>(){{
            put("userid", "1001");
        }});
        XxlApm.report(transaction);

        // heartbeat
        XxlApm.report(new XxlApmHeartbeat());

        // metric
        XxlApm.report(new XxlApmMetric("booking_count"));


        // stop
        TimeUnit.MINUTES.sleep(2);
        xxlApmFactory.stop();
    }

}
