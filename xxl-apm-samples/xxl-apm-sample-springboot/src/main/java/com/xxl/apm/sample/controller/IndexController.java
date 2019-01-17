package com.xxl.apm.sample.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xuxueli 2019-01-17
 */
@Controller
public class IndexController {
    private static Logger logger = LoggerFactory.getLogger(IndexController.class);


    @RequestMapping("")
    @ResponseBody
    public String index(String name){
        if ("error".equals(name)) {
            int error_result = 10/0;
        }
        return "hello world. " + (name!=null?name:"");
    }


}
