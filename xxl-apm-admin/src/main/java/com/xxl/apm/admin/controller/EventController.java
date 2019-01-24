package com.xxl.apm.admin.controller;

import com.xxl.apm.admin.core.model.XxlApmEventReport;
import com.xxl.apm.admin.core.result.ReturnT;
import com.xxl.apm.admin.core.util.DateUtil;
import com.xxl.apm.admin.core.util.JacksonUtil;
import com.xxl.apm.admin.dao.IXxlApmEventReportDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author xuxueli 2019-01-28
 */
@Controller
@RequestMapping("/event")
public class EventController {


    @Resource
    private IXxlApmEventReportDao xxlApmEventReportDao;


    @RequestMapping("")
    public String index(Model model, String querytime, String appname, String type){

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
        List<String> typeList = null;
        if (appname!=null && appname.trim().length()>0) {
            typeList = xxlApmEventReportDao.findTypeList(appname, addtime_from, addtime_to);
        }
        model.addAttribute("typeList", typeList);

        // ip
        type = (type!=null&&typeList!=null&&typeList.contains(type))
                ? type
                : (typeList!=null && typeList.size()>0)
                ?typeList.get(0)
                :null;

        // filter data
        model.addAttribute("querytime", querytime_date);
        model.addAttribute("appname", appname);
        model.addAttribute("type", type);

        // periodSecond
        long periodSecond = (addtime_to<=System.currentTimeMillis())
                ?3600:     // an hour -> second
                (System.currentTimeMillis()-addtime_from)/1000;     // -> second
        model.addAttribute("periodSecond", periodSecond);


        // load data
        List<XxlApmEventReport> eventReportList = new ArrayList<>();
        if (type != null) {
            List<XxlApmEventReport> heartbeatReportList = xxlApmEventReportDao.find(appname, addtime_from, addtime_to, type);
            if (heartbeatReportList!=null && heartbeatReportList.size()>0) {
                for (XxlApmEventReport eventReport: heartbeatReportList) {
                    eventReportList.add(eventReport);
                }
            }
        }
        if (eventReportList.size() > 0) {
            model.addAttribute("eventReportList", JacksonUtil.writeValueAsString(eventReportList));
        }

        return "event/event.index";
    }

    @RequestMapping("/findAppNameList")
    @ResponseBody
    public ReturnT<List<String>> findAppNameList(String appname){
        List<String> appnameList = xxlApmEventReportDao.findAppNameList(appname);
        if (appnameList==null || appnameList.isEmpty()) {
            return new ReturnT<>(ReturnT.FAIL_CODE, null);
        }
        return new ReturnT<>(appnameList);
    }

}
