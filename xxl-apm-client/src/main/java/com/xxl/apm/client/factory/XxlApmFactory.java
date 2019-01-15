package com.xxl.apm.client.factory;

import com.xxl.apm.client.XxlApm;
import com.xxl.apm.client.admin.XxlApmMsgService;
import com.xxl.rpc.registry.impl.XxlRegistryServiceRegistry;
import com.xxl.rpc.remoting.invoker.XxlRpcInvokerFactory;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.serialize.Serializer;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xuxueli 2019-01-15
 */
public class XxlApmFactory {


    // ---------------------- field ----------------------

    private String appname;
    private String adminAddress;
    private String accessToken;

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    // ---------------------- start、stop ----------------------

    public void start(){
        // valid
        if (appname==null || appname.trim().length()==0) {
            throw new RuntimeException("xxl-apm, appname cannot be empty.");
        }

        // start XxlApmMsgService
        startApmMsgService(adminAddress, accessToken);

        // generate XxlApm
        XxlApm.setInstance(this);
    }

    public void stop(){
        // stop XxlApmMsgService
        stopApmMsgService();
    }


    // ---------------------- MsgId ----------------------

    public final ThreadLocal<String> parentMsgId = new ThreadLocal<String>();

    public String generateMsgId(){
        String newMsgId = appname.concat("-").concat(UUID.randomUUID().toString().replaceAll("-", ""));
        return newMsgId;
    }


    // ---------------------- apm msg service ----------------------

    private XxlRpcInvokerFactory xxlRpcInvokerFactory;
    private XxlApmMsgService xxlApmMsgService;

    private ExecutorService clientFactoryThreadPool = Executors.newCachedThreadPool();

    private void startApmMsgService(final String adminAddress, final String accessToken){
        // start invoker factory
        xxlRpcInvokerFactory = new XxlRpcInvokerFactory(XxlRegistryServiceRegistry.class, new HashMap<String, String>(){{
            put(XxlRegistryServiceRegistry.XXL_REGISTRY_ADDRESS, adminAddress);
            put(XxlRegistryServiceRegistry.ACCESS_TOKEN, accessToken);
        }});
        try {
            xxlRpcInvokerFactory.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // apm msg service
        xxlApmMsgService = (XxlApmMsgService) new XxlRpcReferenceBean(
                NetEnum.NETTY,
                Serializer.SerializeEnum.HESSIAN.getSerializer(),
                CallType.SYNC,
                LoadBalance.ROUND,
                XxlApmMsgService.class,
                null,
                10000,
                null,
                null,
                null,
                xxlRpcInvokerFactory).getObject();


        // msg client-consume queue
        for (int i = 0; i < 3; i++) {

        }

    }

    private void stopApmMsgService(){
        // stop invoker factory
        if (xxlRpcInvokerFactory != null) {
            try {
                xxlRpcInvokerFactory.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


}
