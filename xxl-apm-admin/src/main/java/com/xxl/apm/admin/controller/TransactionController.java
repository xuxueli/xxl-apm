package com.xxl.apm.admin.controller;

import com.xxl.apm.admin.core.model.XxlApmHeartbeatReport;
import com.xxl.apm.admin.core.model.XxlApmTransactionReport;
import com.xxl.apm.admin.core.util.CookieUtil;
import com.xxl.apm.admin.core.util.DateUtil;
import com.xxl.apm.admin.core.util.JacksonUtil;
import com.xxl.apm.admin.dao.IXxlApmHeartbeatReportDao;
import com.xxl.apm.admin.dao.IXxlApmTransactionReportDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author xuxueli 2019-01-31
 */
@Controller
@RequestMapping("/transaction")
public class TransactionController {


    @Resource
    private IXxlApmTransactionReportDao xxlApmTransactionReportDao;
    @Resource
    private IXxlApmHeartbeatReportDao xxlApmHeartbeatReportDao;


    @RequestMapping("")
    public String index(Model model, HttpServletRequest request, HttpServletResponse response,
                        String querytime, String appname, String ip,
                        String type){

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
            List<XxlApmHeartbeatReport> ipList = xxlApmHeartbeatReportDao.findAddressList(appname, addtime_from, addtime_to);
            if (ipList!=null && ipList.size()>0) {
                for (XxlApmHeartbeatReport item: ipList) {
                    ipInfo.put(item.getAddress(), item.getAddress().concat("(").concat(item.getHostname()).concat(")") );
                }
            }
        }
        model.addAttribute("ipInfo", ipInfo);

        // ip
        ip = (ip!=null&&ipInfo.containsKey(ip))
                ? ip
                : null;

        // filter data
        model.addAttribute("querytime", querytime_date);
        model.addAttribute("appname", appname);
        model.addAttribute("ip", ip);

        // set cookie
        CookieUtil.set(response, "xxlapm_querytime", querytime, false);
        CookieUtil.set(response, "xxlapm_appname", appname, false);

        // typeList
        List<String> typeList = null;
        if (appname!=null && appname.trim().length()>0) {
            typeList = xxlApmTransactionReportDao.findTypeList(appname, addtime_from, addtime_to);
        }
        model.addAttribute("typeList", typeList);

        // type
        type = (type!=null&&typeList!=null&&typeList.contains(type))
                ? type
                : (typeList!=null && typeList.size()>0)
                ?typeList.get(0)
                :null;
        model.addAttribute("type", type);


        // periodSecond
        long periodSecond = (addtime_to<=System.currentTimeMillis())
                ?3600:     // an hour -> second
                (System.currentTimeMillis()-addtime_from)/1000;     // -> second
        model.addAttribute("periodSecond", periodSecond);


        // load data
        List<XxlApmTransactionReport> reportList = xxlApmTransactionReportDao.find(appname, addtime_from, addtime_to, ip, type);
        if (reportList!=null && reportList.size()>0) {
            model.addAttribute("reportList", JacksonUtil.writeValueAsString(reportList));
        }

        return "transaction/transaction.index";
    }

}
