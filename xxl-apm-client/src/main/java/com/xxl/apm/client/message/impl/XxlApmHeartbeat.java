package com.xxl.apm.client.message.impl;

import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.util.OperatingSystemTool;

import java.util.List;

/**
 * app and machine info, report each minute
 *
 * @author xuxueli 2018-12-29 17:40:15
 */
public class XxlApmHeartbeat extends XxlApmMsg {

    public XxlApmHeartbeat() {
        // pre process
        system_info = new SystemInfo();
        system_info.setProcess_cpu_time(OperatingSystemTool.getOsmxb().getProcessCpuTime());
    }

    // heap, in Mb units, max for used percent
    private MemoryInfo heap_eden_space;
    private MemoryInfo heap_survivor_space;
    private MemoryInfo heap_old_gen;

    // non-heap
    private MemoryInfo non_heap_metaspace;
    private MemoryInfo non_heap_code_cache;
    private MemoryInfo non_heap_compressed_class_space;

    // gc
    private GcInfo gc_marksweep;
    private GcInfo gc_scavenge;

    private GcInfo gc_younggc;
    private GcInfo gc_fullgc;

    // thread
    private List<TheadInfo> thread_list;

    // class
    private ClassInfo class_info;

    // system
    private SystemInfo system_info;


    public MemoryInfo getHeap_eden_space() {
        return heap_eden_space;
    }

    public void setHeap_eden_space(MemoryInfo heap_eden_space) {
        this.heap_eden_space = heap_eden_space;
    }

    public MemoryInfo getHeap_survivor_space() {
        return heap_survivor_space;
    }

    public void setHeap_survivor_space(MemoryInfo heap_survivor_space) {
        this.heap_survivor_space = heap_survivor_space;
    }

    public MemoryInfo getHeap_old_gen() {
        return heap_old_gen;
    }

    public void setHeap_old_gen(MemoryInfo heap_old_gen) {
        this.heap_old_gen = heap_old_gen;
    }

    public MemoryInfo getNon_heap_metaspace() {
        return non_heap_metaspace;
    }

    public void setNon_heap_metaspace(MemoryInfo non_heap_metaspace) {
        this.non_heap_metaspace = non_heap_metaspace;
    }

    public MemoryInfo getNon_heap_code_cache() {
        return non_heap_code_cache;
    }

    public void setNon_heap_code_cache(MemoryInfo non_heap_code_cache) {
        this.non_heap_code_cache = non_heap_code_cache;
    }

    public MemoryInfo getNon_heap_compressed_class_space() {
        return non_heap_compressed_class_space;
    }

    public void setNon_heap_compressed_class_space(MemoryInfo non_heap_compressed_class_space) {
        this.non_heap_compressed_class_space = non_heap_compressed_class_space;
    }

    public GcInfo getGc_marksweep() {
        return gc_marksweep;
    }

    public void setGc_marksweep(GcInfo gc_marksweep) {
        this.gc_marksweep = gc_marksweep;
    }

    public GcInfo getGc_scavenge() {
        return gc_scavenge;
    }

    public void setGc_scavenge(GcInfo gc_scavenge) {
        this.gc_scavenge = gc_scavenge;
    }

    public List<TheadInfo> getThread_list() {
        return thread_list;
    }

    public void setThread_list(List<TheadInfo> thread_list) {
        this.thread_list = thread_list;
    }

    public ClassInfo getClass_info() {
        return class_info;
    }

    public void setClass_info(ClassInfo class_info) {
        this.class_info = class_info;
    }

    public SystemInfo getSystem_info() {
        return system_info;
    }

    public void setSystem_info(SystemInfo system_info) {
        this.system_info = system_info;
    }


    // tool
    @Override
    public void complete() {

        // system
        int kb = 1024;
        int ms_nanoseconds = 1000000;
        //system_info = new SystemInfo();
        system_info.setOs_name(System.getProperty("os.name"));
        system_info.setOs_arch(System.getProperty("os.arch"));
        system_info.setOs_version(System.getProperty("os.version"));
        system_info.setOs_user_name(System.getProperty("user.name"));
        system_info.setJava_home(System.getProperty("java.home"));
        system_info.setJava_version(System.getProperty("java.version"));

        system_info.setCommitted_virtual_memory(OperatingSystemTool.getOsmxb().getCommittedVirtualMemorySize()/kb);
        system_info.setTotal_swap_space(OperatingSystemTool.getOsmxb().getTotalSwapSpaceSize()/kb);
        system_info.setFree_swap_space(OperatingSystemTool.getOsmxb().getFreeSwapSpaceSize()/kb);
        system_info.setTotal_physical_memory(OperatingSystemTool.getOsmxb().getTotalPhysicalMemorySize()/kb);
        system_info.setFree_physical_memory(OperatingSystemTool.getOsmxb().getFreePhysicalMemorySize()/kb);
        system_info.setProcess_cpu_time( (OperatingSystemTool.getOsmxb().getProcessCpuTime()-system_info.getProcess_cpu_time())/ms_nanoseconds);
        system_info.setSystem_cpu_load((float)OperatingSystemTool.getOsmxb().getSystemCpuLoad());
        system_info.setProcess_cpu_load((float)OperatingSystemTool.getOsmxb().getProcessCpuLoad());

        /*long totalMemory = Runtime.getRuntime().totalMemory()/kb;   // 可使用内存
        long freeMemory = Runtime.getRuntime().freeMemory()/kb;     // 剩余内存
        long maxMemory = Runtime.getRuntime().maxMemory()/kb;       // 最大可使用内存*/
    }


    // sub class

    public static class MemoryInfo{
        private float used_space;   // in kb units
        private float total_space;  // to generate used percent

        public MemoryInfo() {
        }
        public MemoryInfo(float used_space, float total_space) {
            this.used_space = used_space;
            this.total_space = total_space;
        }

        public float getUsed_space() {
            return used_space;
        }

        public void setUsed_space(float used_space) {
            this.used_space = used_space;
        }

        public float getTotal_space() {
            return total_space;
        }

        public void setTotal_space(float total_space) {
            this.total_space = total_space;
        }
    }

    public static class GcInfo{
        private int count;
        private float cost;     // in second units

        public GcInfo() {
        }
        public GcInfo(int count, float cost) {
            this.count = count;
            this.cost = cost;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public float getCost() {
            return cost;
        }

        public void setCost(float cost) {
            this.cost = cost;
        }
    }

    public static class TheadInfo{
        private String name;
        private String status;
        private String stack_info;

        public TheadInfo() {
        }
        public TheadInfo(String name, String status, String stack_info) {
            this.name = name;
            this.status = status;
            this.stack_info = stack_info;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStack_info() {
            return stack_info;
        }

        public void setStack_info(String stack_info) {
            this.stack_info = stack_info;
        }
    }

    public static class ClassInfo{
        private int loaded_count;
        private int unload_count;

        public ClassInfo() {
        }
        public ClassInfo(int loaded_count, int unload_count) {
            this.loaded_count = loaded_count;
            this.unload_count = unload_count;
        }

        public int getLoaded_count() {
            return loaded_count;
        }

        public void setLoaded_count(int loaded_count) {
            this.loaded_count = loaded_count;
        }

        public int getUnload_count() {
            return unload_count;
        }

        public void setUnload_count(int unload_count) {
            this.unload_count = unload_count;
        }
    }

    public static class SystemInfo{

        // os
        private String os_name;
        private String os_arch ;
        private String os_version;
        private String os_user_name;
        private String java_home;
        private String java_version;

        // virtual memory
        private long committed_virtual_memory;
        // swap size
        private long total_swap_space;
        private long free_swap_space;
        // physical memory, in km
        private long total_physical_memory;
        private long free_physical_memory;
        // cpu time
        private long process_cpu_time;  // already use cpu time, in ms
        private float system_cpu_load;
        private float process_cpu_load;

        // cpu load
        private int cpu_count;
        private float cpu_load_average_1min;
        private float cpu_load_average_5min;
        private float cpu_load_average_15min;
        private float cpu_used_percent;


        public String getOs_name() {
            return os_name;
        }

        public void setOs_name(String os_name) {
            this.os_name = os_name;
        }

        public String getOs_arch() {
            return os_arch;
        }

        public void setOs_arch(String os_arch) {
            this.os_arch = os_arch;
        }

        public String getOs_version() {
            return os_version;
        }

        public void setOs_version(String os_version) {
            this.os_version = os_version;
        }

        public String getJava_home() {
            return java_home;
        }

        public void setJava_home(String java_home) {
            this.java_home = java_home;
        }

        public String getJava_version() {
            return java_version;
        }

        public void setJava_version(String java_version) {
            this.java_version = java_version;
        }

        public String getOs_user_name() {
            return os_user_name;
        }

        public void setOs_user_name(String os_user_name) {
            this.os_user_name = os_user_name;
        }

        public long getCommitted_virtual_memory() {
            return committed_virtual_memory;
        }

        public void setCommitted_virtual_memory(long committed_virtual_memory) {
            this.committed_virtual_memory = committed_virtual_memory;
        }

        public long getTotal_swap_space() {
            return total_swap_space;
        }

        public void setTotal_swap_space(long total_swap_space) {
            this.total_swap_space = total_swap_space;
        }

        public long getFree_swap_space() {
            return free_swap_space;
        }

        public void setFree_swap_space(long free_swap_space) {
            this.free_swap_space = free_swap_space;
        }

        public long getTotal_physical_memory() {
            return total_physical_memory;
        }

        public void setTotal_physical_memory(long total_physical_memory) {
            this.total_physical_memory = total_physical_memory;
        }

        public long getFree_physical_memory() {
            return free_physical_memory;
        }

        public void setFree_physical_memory(long free_physical_memory) {
            this.free_physical_memory = free_physical_memory;
        }

        public long getProcess_cpu_time() {
            return process_cpu_time;
        }

        public void setProcess_cpu_time(long process_cpu_time) {
            this.process_cpu_time = process_cpu_time;
        }

        public float getSystem_cpu_load() {
            return system_cpu_load;
        }

        public void setSystem_cpu_load(float system_cpu_load) {
            this.system_cpu_load = system_cpu_load;
        }

        public float getProcess_cpu_load() {
            return process_cpu_load;
        }

        public void setProcess_cpu_load(float process_cpu_load) {
            this.process_cpu_load = process_cpu_load;
        }

        public int getCpu_count() {
            return cpu_count;
        }

        public void setCpu_count(int cpu_count) {
            this.cpu_count = cpu_count;
        }

        public float getCpu_load_average_1min() {
            return cpu_load_average_1min;
        }

        public void setCpu_load_average_1min(float cpu_load_average_1min) {
            this.cpu_load_average_1min = cpu_load_average_1min;
        }

        public float getCpu_load_average_5min() {
            return cpu_load_average_5min;
        }

        public void setCpu_load_average_5min(float cpu_load_average_5min) {
            this.cpu_load_average_5min = cpu_load_average_5min;
        }

        public float getCpu_load_average_15min() {
            return cpu_load_average_15min;
        }

        public void setCpu_load_average_15min(float cpu_load_average_15min) {
            this.cpu_load_average_15min = cpu_load_average_15min;
        }

        public float getCpu_used_percent() {
            return cpu_used_percent;
        }

        public void setCpu_used_percent(float cpu_used_percent) {
            this.cpu_used_percent = cpu_used_percent;
        }
    }

}