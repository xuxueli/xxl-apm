# xxl-apm
A distributed APM(application-performance-management) platform.


### todo
- core model
    - admin rpc discovery: xxl-reigtry [done]
    - transfer log: xxl-rpc, async + mult send [ing]
    - log storage: file(client) + es(admin)
    - view: ui  
- BASE: msg type
    - Transaction: type + status + time, qps/99line
    - Event: type + status, qps/suc rate;
    - Heartbeat: app and machine info, time line chart 
    - Metric: biz index, order/booking info, time line
    - Trace: traceId/span
        - tranceId + spanId
- Extend: 
    - Probleam + Alarm:
        - Transaction: 99line error
        - Event: suc rate error
        - Heartbeat: gc error 
        - Metric: order num zero
        - Trace: error invoke