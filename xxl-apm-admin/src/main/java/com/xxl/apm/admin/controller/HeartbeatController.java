package com.xxl.apm.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/heartbeat")
public class HeartbeatController {


    @RequestMapping("")
    public String index(){
        return "heartbeat/heartbeat.index";
    }


}
