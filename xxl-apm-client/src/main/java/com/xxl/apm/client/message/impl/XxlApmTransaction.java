package com.xxl.apm.client.message.impl;

/**
 * transaction msg, like "web/rpc avgline、95line、99line"
 *
 * @author xuxueli 2018-12-29 16:55:14
 */
public class XxlApmTransaction extends XxlApmEvent {


    private long start = System.nanoTime();
    private long cost;       // cost time, in milliseconds


    public XxlApmTransaction() {
    }
    public XxlApmTransaction(String type, String name) {
        super(type, name);
    }


    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }


    // tool
    @Override
    public void complete() {
        if (cost <= 0) {    // not custome, auto calculate
            cost = (System.nanoTime() - start)/1000000;
        }
        start = 0;
    }

}
