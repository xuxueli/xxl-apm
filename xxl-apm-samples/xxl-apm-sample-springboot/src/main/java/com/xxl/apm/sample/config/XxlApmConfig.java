package com.xxl.apm.sample.config;

import com.xxl.apm.client.factory.XxlApmFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xuxueli 2019-01-17
 */
@Configuration
public class XxlApmConfig {


    @Value("${xxl-apm.appname}")
    private String appname;

    @Value("${xxl-apm.adminAddress}")
    private String adminAddress;

    @Value("${xxl-apm.rpc.accessToken}")
    private String accessToken;

    @Value("${xxl-apm.msglog.path}")
    private String msglogpath;


    @Bean(initMethod="start", destroyMethod = "stop")
    public XxlApmFactory xxlApmFactory() {

        XxlApmFactory xxlApmFactory = new XxlApmFactory();
        xxlApmFactory.setAppname(appname);
        xxlApmFactory.setAdminAddress(adminAddress);
        xxlApmFactory.setAccessToken(accessToken);
        xxlApmFactory.setMsglogpath(msglogpath);

        return xxlApmFactory;
    }

}