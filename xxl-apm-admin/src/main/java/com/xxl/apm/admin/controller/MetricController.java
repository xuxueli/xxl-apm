package com.xxl.apm.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author xuxueli 2019-01-28
 */
@Controller
@RequestMapping("/metric")
public class MetricController {


    @RequestMapping("")
    public String index(Model model, String querytime, String appname, String ip){

        return "metric/metric.index";
    }

}
