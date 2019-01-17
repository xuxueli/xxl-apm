package com.xxl.apm.sample.controller;


import com.xxl.apm.client.XxlApm;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmMetric;
import com.xxl.apm.client.message.impl.XxlApmTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2019-01-17
 */
@Controller
public class IndexController {
    private static Logger logger = LoggerFactory.getLogger(IndexController.class);


    @RequestMapping("")
    @ResponseBody
    public String index() throws Exception {

        int randomVal = new Random().nextInt(10);
        int error_result = 10/randomVal;


        /**
         *  1、Transaction 消息
         *
         *      说明："事务" 性质的消息，记录事务（一段逻辑）的运行情况；比如 "TPS、QPS、成功率、响应时间（最大、平均、99.9线、99线line、95line、90线）、运行次数、错误次数……" 等等。
         *      示例：可参考如下示例；也可以参考为Web应用监控原生提供的 "XxlApmWebFilter"；
         */
        XxlApmTransaction transaction = new XxlApmTransaction("demo_random_transaction", String.valueOf(randomVal));
        try {
            TimeUnit.MILLISECONDS.sleep(randomVal);
        } catch (InterruptedException e) {
            transaction.setError(e);
            throw e;
        } finally {
            XxlApm.report(transaction);
        }

        /**
         *  2、Event 消息
         *
         *      说明："事件" 性质的消息，记录一个事件的发生次数；消息结构基本与 "Transaction" 消息一致，仅缺少 "耗时" 属性；如 "失败次数、黑名单拦截此处、非法请求次数" 等；
         *      示例：可参考如下示例；
         */
        XxlApm.report(new XxlApmEvent("demo_random_event", String.valueOf(randomVal)));


        /**
         *  3、Metric 消息
         *
         *      说明："指标" 性质的消息，记录一个指标的数量；如 "在线人数、订单量 …… " 等；
         *      示例：可参考如下示例；
         */
        XxlApm.report(new XxlApmMetric("demo_random_metric"));

        /**
         *  4、Heartbeat 消息
         *
         *      说明：机器和应用JVM信息，心跳方式上报，如 "CPU、Load、磁盘、内存、GC、线程……" 等信息；
         *      示例：接入 XxlApm 之后，原生内置该功能；不需做其他操作；
         */

        return "hello world.";
    }


}
