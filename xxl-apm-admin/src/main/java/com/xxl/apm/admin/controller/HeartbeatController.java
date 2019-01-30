package com.xxl.apm.admin.controller;

import com.xxl.apm.admin.conf.XxlApmMsgServiceImpl;
import com.xxl.apm.admin.core.model.XxlApmHeartbeatReport;
import com.xxl.apm.admin.core.result.ReturnT;
import com.xxl.apm.admin.core.util.CookieUtil;
import com.xxl.apm.admin.core.util.DateUtil;
import com.xxl.apm.admin.core.util.JacksonUtil;
import com.xxl.apm.admin.dao.IXxlApmHeartbeatReportDao;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author xuxueli 2019-01-28
 */
@Controller
@RequestMapping("/heartbeat")
public class HeartbeatController {


    @Resource
    private IXxlApmHeartbeatReportDao xxlApmHeartbeatReportDao;


    @RequestMapping("")
    public String index(Model model, HttpServletRequest request, HttpServletResponse response,
                        String querytime, String appname, String ip){

        // get cookie
        if (querytime == null) {
            String xxlapm_querytime = CookieUtil.getValue(request, "xxlapm_querytime");
            if (xxlapm_querytime != null) {
                querytime = xxlapm_querytime;
            }
        }
        if (appname == null) {
            String xxlapm_appname = CookieUtil.getValue(request, "xxlapm_appname");
            if (xxlapm_appname != null) {
                appname = xxlapm_appname;
            }
        }

        // parse querytime
        Date querytime_date = null;
        if (querytime!=null && querytime.trim().length()>0) {
            querytime_date = DateUtil.parse(querytime, "yyyyMMddHH");
        }
        if (querytime_date == null) {
            querytime_date = DateUtil.parse(DateUtil.format(new Date(), "yyyyMMddHH"), "yyyyMMddHH");
        }
        long addtime_from = querytime_date.getTime();
        long addtime_to = addtime_from + 59*60*1000;    // an hour

        // ipInfo
        Map<String, String> ipInfo = new TreeMap<>();
        if (appname!=null && appname.trim().length()>0) {
            List<XxlApmHeartbeatReport> ipList = xxlApmHeartbeatReportDao.findIpList(appname, addtime_from, addtime_to);
            if (ipList!=null && ipList.size()>0) {
                for (XxlApmHeartbeatReport item: ipList) {
                    ipInfo.put(item.getIp(), item.getIp().concat("(").concat(item.getHostname()).concat(")") );
                }
            }
        }
        model.addAttribute("ipInfo", ipInfo);

        // ip
        ip = (ip!=null&&ipInfo.containsKey(ip))
                ? ip
                : !ipInfo.isEmpty()
                    ?((TreeMap<String, String>) ipInfo).firstKey()
                    :null;

        // filter data
        model.addAttribute("querytime", querytime_date);
        model.addAttribute("appname", appname);
        model.addAttribute("ip", ip);

        // set cookie
        CookieUtil.set(response, "xxlapm_querytime", querytime, false);
        CookieUtil.set(response, "xxlapm_appname", appname, false);


        // load data
        List<XxlApmHeartbeat> heartbeatList = new ArrayList<>();

        if (ip != null) {
            List<XxlApmHeartbeatReport> heartbeatReportList = xxlApmHeartbeatReportDao.find(appname, addtime_from, addtime_to, ip);
            if (heartbeatReportList!=null && heartbeatReportList.size()>0) {
                for (XxlApmHeartbeatReport heartbeatReport: heartbeatReportList) {
                    XxlApmHeartbeat heartbeat = (XxlApmHeartbeat) XxlApmMsgServiceImpl.getSerializer().deserialize(heartbeatReport.getHeartbeat_data(), XxlApmHeartbeat.class);

                    // hide thread stack
                    for (XxlApmHeartbeat.ThreadInfo threadInfo:heartbeat.getThread_list()) {
                        threadInfo.setStack_info(null);
                    };

                    heartbeatList.add(heartbeat);
                }
            }
        }

        if (heartbeatList.size() > 0) {
            model.addAttribute("heartbeatList", JacksonUtil.writeValueAsString(heartbeatList));
        }

        return "heartbeat/heartbeat.index";
    }


    @RequestMapping("/findAppNameList")
    @ResponseBody
    public ReturnT<List<String>> findAppNameList(String appname){
        List<String> appnameList = xxlApmHeartbeatReportDao.findAppNameList(appname);
        if (appnameList==null || appnameList.isEmpty()) {
            return new ReturnT<>(ReturnT.FAIL_CODE, null);
        }
        return new ReturnT<>(appnameList);
    }



    @RequestMapping("/threadinfo")
    public String threadinfo(Model model, HttpServletRequest request, HttpServletResponse response,
                        String querytime, String appname, String ip, @RequestParam(required = false, defaultValue = "-1") int min){

        // get cookie
        if (querytime == null) {
            String xxlapm_querytime = CookieUtil.getValue(request, "xxlapm_querytime");
            if (xxlapm_querytime != null) {
                querytime = xxlapm_querytime;
            }
        }
        if (appname == null) {
            String xxlapm_appname = CookieUtil.getValue(request, "xxlapm_appname");
            if (xxlapm_appname != null) {
                appname = xxlapm_appname;
            }
        }

        // parse querytime
        Date querytime_date = null;
        if (querytime!=null && querytime.trim().length()>0) {
            querytime_date = DateUtil.parse(querytime, "yyyyMMddHH");
        }
        if (querytime_date == null) {
            querytime_date = DateUtil.parse(DateUtil.format(new Date(), "yyyyMMddHH"), "yyyyMMddHH");
        }
        long addtime_from = querytime_date.getTime();
        long addtime_to = addtime_from + 59*60*1000;    // an hour

        // ipInfo
        Map<String, String> ipInfo = new TreeMap<>();
        if (appname!=null && appname.trim().length()>0) {
            List<XxlApmHeartbeatReport> ipList = xxlApmHeartbeatReportDao.findIpList(appname, addtime_from, addtime_to);
            if (ipList!=null && ipList.size()>0) {
                for (XxlApmHeartbeatReport item: ipList) {
                    ipInfo.put(item.getIp(), item.getIp().concat("(").concat(item.getHostname()).concat(")") );
                }
            }
        }
        model.addAttribute("ipInfo", ipInfo);

        // ip
        ip = (ip!=null&&ipInfo.containsKey(ip))
                ? ip
                : !ipInfo.isEmpty()
                ?((TreeMap<String, String>) ipInfo).firstKey()
                :null;

        // filter data
        model.addAttribute("querytime", querytime_date);
        model.addAttribute("appname", appname);
        model.addAttribute("ip", ip);

        // set cookie
        CookieUtil.set(response, "xxlapm_querytime", querytime, false);
        CookieUtil.set(response, "xxlapm_appname", appname, false);

        // min
        min = (min>=0 && min<=59)?min:Calendar.getInstance().get(Calendar.MINUTE);
        model.addAttribute("min", min);

        // load data
        long descTime = addtime_from + min*60*1000;
        List<XxlApmHeartbeatReport> heartbeatReportList = xxlApmHeartbeatReportDao.find(appname, descTime, descTime, ip);
        if (heartbeatReportList!=null && heartbeatReportList.size()>0) {
            XxlApmHeartbeat heartbeat = (XxlApmHeartbeat) XxlApmMsgServiceImpl.getSerializer().deserialize(heartbeatReportList.get(0).getHeartbeat_data(), XxlApmHeartbeat.class);
            List<XxlApmHeartbeat.ThreadInfo> threadInfoList = heartbeat.getThread_list();
            model.addAttribute("threadInfoList", threadInfoList);
        }

        return "heartbeat/threadinfo.index";
    }

}
