package com.xxl.apm.client.message.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * transaction msg, like "web/rpc avgline、95line、99line"
 *
 * @author xuxueli 2018-12-29 16:55:14
 */
public class XxlApmTransaction extends XxlApmEvent {


    private long time = System.nanoTime();      // cost time, in milliseconds

    private long time_max;
    private long time_avg;
    private long time_tp90;
    private long time_tp95;
    private long time_tp99;
    private long time_tp999;

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

    public long getTime_max() {
        return time_max;
    }

    public void setTime_max(long time_max) {
        this.time_max = time_max;
    }

    public long getTime_avg() {
        return time_avg;
    }

    public void setTime_avg(long time_avg) {
        this.time_avg = time_avg;
    }

    public long getTime_tp90() {
        return time_tp90;
    }

    public void setTime_tp90(long time_tp90) {
        this.time_tp90 = time_tp90;
    }

    public long getTime_tp95() {
        return time_tp95;
    }

    public void setTime_tp95(long time_tp95) {
        this.time_tp95 = time_tp95;
    }

    public long getTime_tp99() {
        return time_tp99;
    }

    public void setTime_tp99(long time_tp99) {
        this.time_tp99 = time_tp99;
    }

    public long getTime_tp999() {
        return time_tp999;
    }

    public void setTime_tp999(long time_tp999) {
        this.time_tp999 = time_tp999;
    }


    // tool
    @Override
    public void complete() {
        super.complete();

        // etc
        int ms_nanoseconds = 1000000;
        this.time = (System.nanoTime() - time)/ms_nanoseconds;

        computingTP(this);
    }


    private static transient long period = 0;
    private static transient Map<String, List<Long>> periodTPMap = new ConcurrentHashMap<>();
    private static long getNowPeriod(){
        long min = (System.currentTimeMillis()/60000)*60000;;
        return min;
    }
    private static long tp(List<Long> times, float percent) {
        float percentF = percent/100;
        Collections.sort(times);

        int index = (int)(percentF * times.size() - 1);
        return times.get(index);
    }
    private static void computingTP(XxlApmTransaction transaction){

        // addtime -> min
        long min = (transaction.getAddtime()/60000)*60000;

        // match report key
        String transactionKey = transaction.getAppname()
                .concat(String.valueOf(min))
                .concat(transaction.getAddress())
                .concat(transaction.getType())
                .concat(transaction.getName());

        // valid period
        if (min != period) {
            periodTPMap.clear();
            period = min;
        }

        // valid time
        List<Long> timeList = periodTPMap.get(transactionKey);
        if (timeList == null) {
            timeList = new ArrayList<>();
            periodTPMap.put(transactionKey, timeList);
        }

        // computing
        timeList.add(transaction.getTime());

        long totalTime = 0;
        long maxTime = 0;
        for (long item:timeList) {
            totalTime += item;
            if (item>maxTime) {
                maxTime = item;
            }
        }

        transaction.setTime_max(maxTime);
        transaction.setTime_avg( totalTime/timeList.size() );
        transaction.setTime_tp90( tp(timeList, 90f) );
        transaction.setTime_tp95( tp(timeList, 95f) );
        transaction.setTime_tp99( tp(timeList, 99f) );
        transaction.setTime_tp999( tp(timeList, 99.9f) );

    }

}
