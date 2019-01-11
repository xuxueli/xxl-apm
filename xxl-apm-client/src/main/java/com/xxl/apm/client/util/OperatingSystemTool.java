package com.xxl.apm.client.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

/**
 * @author xuxueli 2019-01-11
 */
public class OperatingSystemTool {

    private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static OperatingSystemMXBean getOsmxb() {
        return osmxb;
    }

}
