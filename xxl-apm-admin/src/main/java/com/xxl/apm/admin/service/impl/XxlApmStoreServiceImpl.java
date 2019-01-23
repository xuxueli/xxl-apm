package com.xxl.apm.admin.service.impl;

import com.xxl.apm.admin.conf.XxlApmMsgServiceImpl;
import com.xxl.apm.admin.core.model.XxlApmHeartbeatReport;
import com.xxl.apm.admin.dao.IXxlApmHeartbeatReportDao;
import com.xxl.apm.admin.service.XxlApmStoreService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import com.xxl.apm.client.message.impl.XxlApmTransaction;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author xuxueli 2019-01-17
 */
@Service
public class XxlApmStoreServiceImpl implements XxlApmStoreService {


    @Resource
    private IXxlApmHeartbeatReportDao xxlApmHeartbeatReportDao;


    @Override
    public boolean processMsg(List<XxlApmMsg> messageList) {

        // dispatch msg
        List<XxlApmEvent> eventList = null;
        List<XxlApmTransaction> transactionList = null;
        List<XxlApmHeartbeat> heartbeatList = null;

        for (XxlApmMsg apmMsg: messageList) {
            if (apmMsg instanceof XxlApmEvent) {
                if (eventList == null) {
                    eventList = new ArrayList<>();
                }
                eventList.add((XxlApmEvent) apmMsg);
            } else if (apmMsg instanceof XxlApmTransaction) {
                if (transactionList == null) {
                    transactionList = new ArrayList<>();
                }
                transactionList.add((XxlApmTransaction) apmMsg);
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
            for (XxlApmEvent event:eventList) {

            }
        }

        /**
         * todo-apm:
         *
         * fresh report
         * store logView
         *
         *
         * 汇总到 hour 维度
         * LogView 汇总写文件；关联到Report上；
         */

        return false;
    }

    @Override
    public boolean cleanMsg(int msglogStorageDay) {

        // timeout time
        Calendar timeoutCal = Calendar.getInstance();
        timeoutCal.add(Calendar.DAY_OF_MONTH, -1*msglogStorageDay);
        long timeoutTime = timeoutCal.getTimeInMillis();

        // clean heatbeat report
        xxlApmHeartbeatReportDao.clean(timeoutTime);


        /**
         * todo-apm:
         *
         * clean old report + logView
         */

        return false;
    }

}
