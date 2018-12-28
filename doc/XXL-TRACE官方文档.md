# xxl-trace
A distributed tracing and apm platform.


### todo
- core model
    - transfer log: xxl-rpc (async + mult send)
    - admin rpc discovery: xxl-reigtry
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