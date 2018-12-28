# xxl-apm
A distributed APM(application-performance-management) platform.


### todo
- core model
    - admin rpc discovery: xxl-reigtry [done]
    - transfer log: xxl-rpc (async + mult send)
    - log storage: file(client) + es(admin)
    - view: ui 
- trace
    - tranceId
- apm
    - 
- msg type
    - Transaction: event + time, 99line/avg
    - Event: event, 99line/avg
    - Heartbeat: app and machine info, time line chart 
    - Metpanric: biz index, avg/time line
    - Trace: traceId/span
- p
        - Probleam: 
        - Alarm