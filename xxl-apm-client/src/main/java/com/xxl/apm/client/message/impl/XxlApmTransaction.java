package com.xxl.apm.client.message.impl;

/**
 * transaction msg, like "web/rpc avgline、95line、99line"
 *
 * @author xuxueli 2018-12-29 16:55:14
 */
public class XxlApmTransaction extends XxlApmEvent {


    private long time = System.nanoTime();      // cost time, in milliseconds


    public XxlApmTransaction() {
    }
    public XxlApmTransaction(String type, String name) {
        super(type, name);
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    // tool
    @Override
    public void complete() {
        int ms_nanoseconds = 1000000;
        this.time = (System.nanoTime() - time)/ms_nanoseconds;
    }

}
