package com.xxl.apm.admin.conf;

import com.xxl.apm.client.admin.ApmAdminService;
import com.xxl.apm.client.admin.param.XxlApmMsg;
import com.xxl.apm.client.admin.param.XxlApmMsgResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
@Component
public class XxlApmAdminServiceImpl implements ApmAdminService, InitializingBean, DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(XxlApmAdminServiceImpl.class);


    @Value("${xxl-apm.rpc.remoting.ip}")
    private String ip;
    @Value("${xxl-apm.rpc.remoting.port}")
    private int port;
    @Value("${xxl-apm.log.logretentiondays}")
    private int logretentiondays;


    // ---------------------- start stop ----------------------

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }


    // ---------------------- service ----------------------

    @Override
    public XxlApmMsgResult report(List<XxlApmMsg> msgList) {

        return new XxlApmMsgResult();
    }

}
