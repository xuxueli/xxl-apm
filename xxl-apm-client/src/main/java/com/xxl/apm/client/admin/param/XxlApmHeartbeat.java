package com.xxl.apm.client.admin.param;

/**
 * app and machine info, report each minute
 *
 * @author xuxueli 2018-12-29 17:40:15
 */
public class XxlApmHeartbeat extends XxlApmMsg {


    // heap, in Mb units, max for used percent
    private float heap_memory_eden_space;
    private float heap_memory_survivor_space;
    private float heap_memory_old_gen;

    private float heap_memory_max_eden_space;
    private float heap_memory_max_survivor_space;
    private float heap_memory_max_old_gen;

    // non-heap
    private float non_heap_memory_metaspace;
    private float non_heap_memory_code_cache;
    private float non_heap_memory_compressed_class_space;

    private float non_heap_memory_max_metaspace;
    private float non_heap_memory_max_code_cache;
    private float non_heap_memory_max_compressed_class_space;

    // gc
    private int gc_marksweep;
    private int gc_scavenge;

    // thread
    private int thread_count_max;
    private int thread_active_count;

    // class
    private int class_load_count;
    private int class_unload_count;

    // system
    private float system_load;
    private float system_cpu;
    private float system_ds;

    private float system_ds_max;

}