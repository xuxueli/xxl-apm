package com.xxl.apm.admin.controller;

import com.xxl.apm.admin.core.util.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Date;

/**
 * @author xuxueli 2019-01-28
 */
@Controller
@RequestMapping("/metric")
public class MetricController {


    @RequestMapping("")
    public String index(Model model, String querytime, String appname, String name){

        // parse querytime
        Date querytime_date = null;
        if (querytime!=null && querytime.trim().length()>0) {
            querytime_date = DateUtil.parse(querytime, "yyyyMMddHH");
        }
        if (querytime_date == null) {
            querytime_date = DateUtil.parse(DateUtil.format(new Date(), "yyyyMMddHH"), "yyyyMMddHH");
        }

        // load data
        model.addAttribute("nameList", Arrays.asList("demo-metic-1", "demo-metic-2"));

        // parse data
        model.addAttribute("querytime", querytime_date);
        model.addAttribute("appname", appname);
        model.addAttribute("name", name);

        return "metric/metric.index";
    }

}
