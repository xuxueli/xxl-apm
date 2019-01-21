package com.xxl.apm.admin.controller;

import com.xxl.apm.admin.conf.XxlApmMsgServiceImpl;
import com.xxl.apm.admin.core.model.XxlApmHeartbeatReport;
import com.xxl.apm.admin.core.util.DateUtil;
import com.xxl.apm.admin.dao.IXxlApmHeartbeatReportDao;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/heartbeat")
public class HeartbeatController {


    @Resource
    private IXxlApmHeartbeatReportDao xxlApmHeartbeatReportDao;


    @RequestMapping("")
    public String index(Model model, String querytime, String appname, String ip){

        // parse param
        Date querytime_date = null;
        if (querytime!=null && querytime.trim().length()>0) {
            querytime_date = DateUtil.parse(querytime, "yyyyMMddHH");
        }
        if (querytime_date == null) {
            querytime_date = DateUtil.parse(DateUtil.format(new Date(), "yyyyMMddHH"), "yyyyMMddHH");
        }
        long addtime_from = 0;
        long addtime_to = 0;
        if (appname!=null && appname.trim().length()>0 && ip!=null && ip.trim().length()>0) {
            addtime_from = querytime_date.getTime();
            addtime_to = addtime_from + 60*60*1000;
        }

        // load data
        List<XxlApmHeartbeat> heartbeatList = new ArrayList<>();
        if (addtime_from > 0) {
            List<XxlApmHeartbeatReport> heartbeatReportList = xxlApmHeartbeatReportDao.find(appname, addtime_from, addtime_to, ip);
            if (heartbeatReportList!=null && heartbeatReportList.size()>0) {
                for (XxlApmHeartbeatReport heartbeatReport: heartbeatReportList) {
                    XxlApmHeartbeat heartbeat = (XxlApmHeartbeat) XxlApmMsgServiceImpl.getSerializer().deserialize(heartbeatReport.getHeartbeat_data(), XxlApmHeartbeat.class);
                    heartbeatList.add(heartbeat);
                }
            }
        }

        // parse data
        model.addAttribute("querytime", querytime_date);
        model.addAttribute("appname", appname);
        model.addAttribute("ip", ip);

        return "heartbeat/heartbeat.index";
    }


}
