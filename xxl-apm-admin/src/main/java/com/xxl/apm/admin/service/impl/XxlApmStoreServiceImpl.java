package com.xxl.apm.admin.service.impl;

import com.xxl.apm.admin.conf.XxlApmMsgServiceImpl;
import com.xxl.apm.admin.core.model.XxlApmEventReport;
import com.xxl.apm.admin.core.model.XxlApmHeartbeatReport;
import com.xxl.apm.admin.dao.IXxlApmEventReportDao;
import com.xxl.apm.admin.dao.IXxlApmHeartbeatReportDao;
import com.xxl.apm.admin.service.XxlApmStoreService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import com.xxl.apm.client.message.impl.XxlApmTransaction;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author xuxueli 2019-01-17
 */
@Service
public class XxlApmStoreServiceImpl implements XxlApmStoreService {


    @Resource
    private IXxlApmHeartbeatReportDao xxlApmHeartbeatReportDao;
    @Resource
    private IXxlApmEventReportDao xxlApmEventReportDao;


    @Override
    public boolean processMsg(List<XxlApmMsg> messageList) {

        // dispatch msg
        List<XxlApmHeartbeat> heartbeatList = null;
        List<XxlApmTransaction> transactionList = null;
        List<XxlApmEvent> eventList = null;

        for (XxlApmMsg apmMsg: messageList) {
            if (apmMsg instanceof XxlApmTransaction) {
                if (transactionList == null) {
                    transactionList = new ArrayList<>();
                }
                transactionList.add((XxlApmTransaction) apmMsg);
            } else if (apmMsg instanceof XxlApmEvent) {
                if (eventList == null) {
                    eventList = new ArrayList<>();
                }
                eventList.add((XxlApmEvent) apmMsg);
            } else if (apmMsg instanceof XxlApmHeartbeat) {
                if (heartbeatList == null) {
                    heartbeatList = new ArrayList<>();
                }
                heartbeatList.add((XxlApmHeartbeat) apmMsg);
            }
        }

        // dispatch process
        if (heartbeatList!=null && heartbeatList.size() > 0) {

            List<XxlApmHeartbeatReport> heartbeatReportList = new ArrayList<>();
            for (XxlApmHeartbeat heartbeat: heartbeatList) {

                // addtime -> min
                heartbeat.setAddtime((heartbeat.getAddtime()/60000)*60000);
                byte[] heartbeat_data = XxlApmMsgServiceImpl.getSerializer().serialize(heartbeat);

                XxlApmHeartbeatReport heartbeatReport = new XxlApmHeartbeatReport();
                heartbeatReport.setAppname(heartbeat.getAppname());
                heartbeatReport.setAddtime(heartbeat.getAddtime());
                heartbeatReport.setIp(heartbeat.getIp());
                heartbeatReport.setHostname(heartbeat.getHostname());

                heartbeatReport.setHeartbeat_data(heartbeat_data);

                heartbeatReportList.add(heartbeatReport);
            }

            xxlApmHeartbeatReportDao.addMult(heartbeatReportList);  // for minute
        }

        if (eventList!=null && eventList.size()>0) {

            Map<String, XxlApmEventReport> eventMap = new HashMap<>();
            for (XxlApmEvent event:eventList) {

                // addtime -> min
                event.setAddtime((event.getAddtime()/60000)*60000);

                // match event data
                String eventKey = event.getAppname()
                        .concat(String.valueOf(event.getAddtime()))
                        .concat(event.getIp())
                        .concat(event.getType())
                        .concat(event.getName());
                boolean success = XxlApmEvent.SUCCESS_STATUS.equals(event.getStatus());

                XxlApmEventReport eventReport = eventMap.get(eventKey);
                if (eventReport == null) {
                    eventReport = new XxlApmEventReport();
                    eventReport.setAppname(event.getAppname());
                    eventReport.setAddtime(event.getAddtime());
                    eventReport.setIp(event.getIp());
                    eventReport.setHostname(event.getHostname());

                    eventReport.setType(event.getType());
                    eventReport.setName(event.getName());

                    eventMap.put(eventKey, eventReport);
                }

                eventReport.setTotal_count(eventReport.getTotal_count() + 1);
                if (!success) {
                    eventReport.setFail_count(eventReport.getFail_count() + 1);
                }

                // todo, logview
            }

            for (XxlApmEventReport eventReport: eventMap.values()) {
                int ret = xxlApmEventReportDao.update(eventReport);
                if (ret < 1) {
                    xxlApmEventReportDao.add(eventReport);
                }
            }
            
        }

        /**
         * todo-apm:
         *
         * - fresh report, in min (report simple data)
         * - store logView (store file or es)
         * - Report bind LogView, LogView write file
         * - Error LogView, Alerm;
         */

        return false;
    }

    @Override
    public boolean cleanMsg(int msglogStorageDay) {

        // timeout time
        Calendar timeoutCal = Calendar.getInstance();
        timeoutCal.add(Calendar.DAY_OF_MONTH, -1*msglogStorageDay);
        long timeoutTime = timeoutCal.getTimeInMillis();

        // clean timeout report
        xxlApmHeartbeatReportDao.clean(timeoutTime);
        xxlApmEventReportDao.clean(timeoutTime);

        /**
         * todo-apm:
         *
         * clean old report + logView
         */

        return false;
    }

}
