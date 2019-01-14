# xxl-apm
A distributed APM(application-performance-management) platform.


### todo
- core model
    - admin rpc discovery: xxl-reigtry [done]
    - transfer log: xxl-rpc, async + mult send [ing]
    - log storage: file(client) + es(admin)
    - view: ui  
- BASE: msg type
    - Event: type + status, qps/suc rate;
    - Transaction: type + status + time, qps/99line
    - Heartbeat: app and machine info, time line chart 
    - Metric: biz index, order/booking info, time line
    - Trace: extands Transaction
        - parent_trance_id: zero as root
        - trance_id: 
- Extend: 
    - Probleam + Alarm:
        - Transaction: 99line error
        - Event: suc rate error
        - Heartbeat: gc error 
        - Metric: order num zero
        - Trace: error invoke
        
- other
    - api：tcp + http
    - mvc、rpc、db、cache
    - trace拓扑
    - 机器指标，时间折线图；
