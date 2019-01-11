package com.xxl.apm.client.os;

import com.xxl.apm.client.os.impl.JdkOsHelper;
import com.xxl.apm.client.os.impl.SunOsHelper;

/**
 * @author xuxueli 2019-01-11
 */
public abstract class OsHelper {

    private static OsHelper instance = null;
    public static OsHelper getInstance() {
        return instance;
    }
    static {
        try {
            Class.forName("com.sun.management.OperatingSystemMXBean");  // remove strong dependency on oracle jdk
            instance = new SunOsHelper();
        } catch (ClassNotFoundException e) {
        }
        if (instance == null) {
            instance = new JdkOsHelper();
        }
    }

    public abstract long getCommittedVirtualMemorySize();

    public abstract long getTotalSwapSpaceSize();

    public abstract long getFreeSwapSpaceSize();

    public abstract long getProcessCpuTime();

    public abstract long getFreePhysicalMemorySize();

    public abstract long getTotalPhysicalMemorySize();

    public abstract double getSystemCpuLoad();

    public abstract double getProcessCpuLoad();

}
