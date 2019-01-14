# xxl-apm
A distributed APM(application-performance-management) platform.


### todo
- core model
    - admin rpc discovery: xxl-reigtry [done]
    - transfer log: xxl-rpc, async + mult send [ing]
    - log storage: file(client) + es(admin)
    - view: ui  
- Msg: msg type [done]
    - [Trace]: ApmMsg all support [done]
        - parentMsgId: null as root
        - msgId: appname-uuid
    - Event: type + status, qps/suc rate;
    - Transaction: type + status + time, qps/99line
    - Heartbeat: app and machine info, time line chart 
    - Metric: biz index, order/booking info, time line
- Probleam: 
    - [Trace]: by msg impl;
    - Event: threshold for 'suc rate'
    - Transaction: threshold for 'suc rate'、'99line long-rpc.service/long-rpc.client'
    - Heartbeat: gc
    - Metric: order num zero
- Alarm: bind with appname
    - Email: email
    - WebHook: http://.....{error_msg}...
- other
    - api：tcp + http
    - mvc、rpc、db、cache
    - trace拓扑
    - 机器指标，时间折线图；
