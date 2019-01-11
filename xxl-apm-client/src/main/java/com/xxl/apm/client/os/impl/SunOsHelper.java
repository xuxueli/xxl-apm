package com.xxl.apm.client.os.impl;

import com.sun.management.OperatingSystemMXBean;
import com.xxl.apm.client.os.OsHelper;

import java.lang.management.ManagementFactory;

/**
 * @author xuxueli 2019-01-11
 */
public class SunOsHelper extends OsHelper  {

    private OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();


    @Override
    public long getCommittedVirtualMemorySize() {
        return osmxb.getCommittedVirtualMemorySize();
    }

    @Override
    public long getTotalSwapSpaceSize() {
        return osmxb.getTotalSwapSpaceSize();
    }

    @Override
    public long getFreeSwapSpaceSize() {
        return osmxb.getFreeSwapSpaceSize();
    }

    @Override
    public long getProcessCpuTime() {
        return osmxb.getProcessCpuTime();
    }

    @Override
    public long getFreePhysicalMemorySize() {
        return osmxb.getFreePhysicalMemorySize();
    }

    @Override
    public long getTotalPhysicalMemorySize() {
        return osmxb.getTotalPhysicalMemorySize();
    }

    @Override
    public double getSystemCpuLoad() {
        return osmxb.getSystemCpuLoad();
    }

    @Override
    public double getProcessCpuLoad() {
        return osmxb.getProcessCpuLoad();
    }

}
