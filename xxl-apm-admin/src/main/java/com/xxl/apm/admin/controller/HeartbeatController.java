package com.xxl.apm.admin.controller;

import com.xxl.apm.admin.conf.XxlApmMsgServiceImpl;
import com.xxl.apm.admin.core.model.XxlApmHeartbeatReport;
import com.xxl.apm.admin.core.util.DateUtil;
import com.xxl.apm.admin.core.util.JacksonUtil;
import com.xxl.apm.admin.dao.IXxlApmHeartbeatReportDao;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/heartbeat")
public class HeartbeatController {


    @Resource
    private IXxlApmHeartbeatReportDao xxlApmHeartbeatReportDao;


    @RequestMapping("")
    public String index(Model model, String querytime, String appname, String ip){

        // parse querytime
        Date querytime_date = null;
        if (querytime!=null && querytime.trim().length()>0) {
            querytime_date = DateUtil.parse(querytime, "yyyyMMddHH");
        }
        if (querytime_date == null) {
            querytime_date = DateUtil.parse(DateUtil.format(new Date(), "yyyyMMddHH"), "yyyyMMddHH");
        }

        // load by appname
        List<XxlApmHeartbeat> heartbeatList = new ArrayList<>();
        Map<String, String> ipInfo = new TreeMap<>();

        if (appname!=null && appname.trim().length()>0) {
            long addtime_from = querytime_date.getTime();
            long addtime_to = addtime_from + 60*60*1000;
            List<XxlApmHeartbeatReport> heartbeatReportList = xxlApmHeartbeatReportDao.find(appname, addtime_from, addtime_to, ip);
            if (heartbeatReportList!=null && heartbeatReportList.size()>0) {
                for (XxlApmHeartbeatReport heartbeatReport: heartbeatReportList) {
                    XxlApmHeartbeat heartbeat = (XxlApmHeartbeat) XxlApmMsgServiceImpl.getSerializer().deserialize(heartbeatReport.getHeartbeat_data(), XxlApmHeartbeat.class);
                    heartbeatList.add(heartbeat);

                    ipInfo.put(heartbeat.getIp(), heartbeat.getIp().concat("(").concat(heartbeat.getHostname()).concat(")") );
                }
            }
        }

        // filter by ip
        if (heartbeatList.size() > 0) {
            if (ip==null || ip.trim().length()==0 || !ipInfo.containsKey(ip)) {
                ip = ((TreeMap<String, String>) ipInfo).firstKey();
            }
            List<XxlApmHeartbeat> heartbeatList_filter_ip = new ArrayList<>();
            for (XxlApmHeartbeat item: heartbeatList) {
                if (item.getIp().equals(ip)) {
                    heartbeatList_filter_ip.add(item);
                }
            }
            if (heartbeatList_filter_ip.size() > 0) {
                model.addAttribute("heartbeatList", JacksonUtil.writeValueAsString(heartbeatList));
            }
        }

        // parse data
        model.addAttribute("querytime", querytime_date);
        model.addAttribute("appname", appname);
        model.addAttribute("ip", ip);

        return "heartbeat/heartbeat.index";
    }


}
