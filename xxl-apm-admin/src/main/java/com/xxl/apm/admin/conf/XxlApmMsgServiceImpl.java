package com.xxl.apm.admin.conf;

import com.xxl.apm.admin.core.model.XxlCommonRegistryData;
import com.xxl.apm.admin.service.impl.XxlCommonRegistryServiceImpl;
import com.xxl.apm.client.admin.XxlApmMsgService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.registry.client.util.json.BasicJson;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.remoting.provider.XxlRpcProviderFactory;
import com.xxl.rpc.serialize.Serializer;
import com.xxl.rpc.util.IpUtil;
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
public class XxlApmMsgServiceImpl implements XxlApmMsgService, InitializingBean, DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(XxlApmMsgServiceImpl.class);


    @Value("${xxl-apm.rpc.remoting.ip}")
    private String ip;
    @Value("${xxl-apm.rpc.remoting.port}")
    private int port;


    // ---------------------- start stop ----------------------

    @Override
    public void afterPropertiesSet() throws Exception {
        initServer();
    }

    @Override
    public void destroy() throws Exception {
        destoryServer();
    }


    // ---------------------- apm server ----------------------

    private XxlRpcProviderFactory providerFactory;

    public void initServer() throws Exception {

        // address, static registry
        ip = (ip!=null&&ip.trim().length()>0)?ip:IpUtil.getIp();
        String address = IpUtil.getIpPort(ip, port);

        XxlCommonRegistryData xxlCommonRegistryData = new XxlCommonRegistryData();
        xxlCommonRegistryData.setKey(XxlApmMsgService.class.getName());
        xxlCommonRegistryData.setValue(address);
        XxlCommonRegistryServiceImpl.staticRegistryData = xxlCommonRegistryData;


        // init server
        providerFactory = new XxlRpcProviderFactory();
        providerFactory.initConfig(NetEnum.NETTY, Serializer.SerializeEnum.HESSIAN.getSerializer(), ip, port, null, null, null);

        // add server
        providerFactory.addService(XxlApmMsgService.class.getName(), null, this);

        // start server
        providerFactory.start();
    }

    public void destoryServer() throws Exception {
        // stop server
        if (providerFactory != null) {
            providerFactory.stop();
        }
    }


    // ---------------------- service ----------------------

    @Override
    public boolean beat() {
        return true;
    }

    @Override
    public boolean report(List<XxlApmMsg> msgList) {

        /**
         * todo:
         * consume msg
         *
         *  - async queue process; 200 msg per take
         *  - queue-max or fail, write file
         *  - fail-retry thread, read file
         *
         *  - async thead:
         *      - 1min-thread: heartbeat
         *      - real-time-thread: event、transaction、metric
         */

        System.out.println(BasicJson.toJson(msgList));

        return true;
    }

}
