package com.xxl.apm.admin.service.impl;

import com.xxl.apm.admin.core.model.XxlApmHeartbeatReport;
import com.xxl.apm.admin.service.XxlApmStoreService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;

import java.util.List;

public class XxlApmStoreServiceImpl implements XxlApmStoreService {

    @Override
    public boolean processMsg(List<XxlApmMsg> messageList) {

        /**
         * todo:
         *
         * fresh report
         *
         * store logView
         */

        for (XxlApmMsg apmMsg : messageList) {
            if (apmMsg instanceof XxlApmHeartbeat) {

                XxlApmHeartbeatReport heartbeatReport = new XxlApmHeartbeatReport();
                //heartbeatReport.setPeriod();

                // 汇总到 min 维度

                // heartbeat report
                // thread report

            } else if (apmMsg instanceof XxlApmEvent) {

                /**
                 * 汇总到 hour 维度
                 * LogView 汇总写文件；关联到Report上；
                 *
                 */
            }
        }


        return false;
    }

    @Override
    public boolean cleanMsg(int msglogStorageDay) {

        /**
         * todo:
         *
         * clean old report + logView
         */

        return false;
    }

}
