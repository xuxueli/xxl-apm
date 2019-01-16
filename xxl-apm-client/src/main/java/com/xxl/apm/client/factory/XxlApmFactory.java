package com.xxl.apm.client.factory;

import com.xxl.apm.client.XxlApm;
import com.xxl.apm.client.admin.XxlApmMsgService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.util.FileUtil;
import com.xxl.registry.client.util.json.BasicJson;
import com.xxl.rpc.registry.impl.XxlRegistryServiceRegistry;
import com.xxl.rpc.remoting.invoker.XxlRpcInvokerFactory;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2019-01-15
 */
public class XxlApmFactory {
    private static final Logger logger = LoggerFactory.getLogger(XxlApmFactory.class);

    // ---------------------- field ----------------------

    private String appname;
    private String adminAddress;
    private String accessToken;
    private String msglogpath = "/data/applogs/xxl-apm/msglogpath";

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setMsglogpath(String msglogpath) {
        this.msglogpath = msglogpath;
    }

    // ---------------------- start、stop ----------------------

    public void start(){
        // valid
        if (appname==null || appname.trim().length()==0) {
            throw new RuntimeException("xxl-apm, appname cannot be empty.");
        }
        if (msglogpath==null || msglogpath.trim().length()==0) {
            throw new RuntimeException("xxl-apm, msglogpath cannot be empty.");
        }

        // msglogpath
        File msglogpathDir = new File(msglogpath);
        if (!msglogpathDir.exists()) {
            msglogpathDir.mkdirs();
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
    public volatile boolean clientFactoryPoolStoped = false;
    private LinkedBlockingQueue<XxlApmMsg> newMessageQueue = new LinkedBlockingQueue<>();

    /**
     * async report msg
     *
     * @param msgList
     * @return
     */
    public boolean report(List<XxlApmMsg> msgList) {

        // append queue
        newMessageQueue.addAll(msgList);

        // valid queue-max
        if (newMessageQueue.size() > 100000) {
            List<XxlApmMsg> messageList = new ArrayList<>();
            int drainToNum = newMessageQueue.drainTo(messageList, (200+msgList.size()) );
            if (messageList.size() > 0) {
                writeMsgFile(messageList);    // queue-max or report-fail, write file
            }
        }

        return true;
    }

    // msg-file
    private boolean writeMsgFile(List<XxlApmMsg> msgList){
        String msgListJson = BasicJson.toJson(msgList);
        String msgListFileName = msglogpath.concat(File.separator).concat(msgList.get(0).getMsgId());
        FileUtil.writeFileContent(msgListFileName, msgListJson);
        return true;
    }

    // start stop
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


        // start msg-queue thread
        for (int i = 0; i < 5; i++) {
            clientFactoryThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    while (!clientFactoryPoolStoped) {
                        List<XxlApmMsg> messageList = null;
                        try {
                            XxlApmMsg message = newMessageQueue.take();
                            if (message != null) {

                                // load
                                messageList = new ArrayList<>();
                                messageList.add(message);

                                List<XxlApmMsg> otherMessageList = new ArrayList<>();
                                int drainToNum = newMessageQueue.drainTo(otherMessageList, 200);
                                if (drainToNum > 0) {
                                    messageList.addAll(otherMessageList);
                                }

                                // report
                                boolean ret = xxlApmMsgService.report(messageList);
                                if (ret) {
                                    messageList.clear();
                                }

                            }
                        } catch (Exception e) {
                            if (!clientFactoryPoolStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        } finally {
                            // report fail, write msg-file
                            if (messageList!=null && messageList.size()>0) {
                                writeMsgFile(messageList);    // queue-max or report-fail, write file
                                messageList.clear();
                            }
                        }
                    }

                    // finally total
                    List<XxlApmMsg> messageList = new ArrayList<>();
                    int drainToNum = newMessageQueue.drainTo(messageList);
                    if (drainToNum> 0) {
                        boolean ret = xxlApmMsgService.report(messageList);
                        if (!ret) {
                            writeMsgFile(messageList);    // queue-max or report-fail, write msg-file
                            messageList.clear();
                        }
                    }

                }
            });
        }

        // start msg-file thread
        clientFactoryThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                while (!clientFactoryPoolStoped) {

                    int waitTim = 5;
                    try {

                        File file = new File(msglogpath);
                        if (file.listFiles()!=null && file.listFiles().length>0) {
                            waitTim = 5;
                            for (File fileItem : file.listFiles()) {

                                try {
                                    // read msg-file
                                    String msgListJson = FileUtil.readFileContent(fileItem);
                                    List<XxlApmMsg> messageList = BasicJson.parseList(msgListJson, XxlApmMsg.class);

                                    // retry report
                                    boolean ret = xxlApmMsgService.report(messageList);

                                    // delete
                                    if (ret) {
                                        fileItem.delete();
                                    }
                                } catch (Exception e) {
                                    if (!clientFactoryPoolStoped) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }

                            }


                        } else {
                            waitTim = (waitTim+5<=60)?(waitTim+5):60;
                        }

                    } catch (Exception e) {
                        if (!clientFactoryPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }

                    }

                    // wait
                    try {
                        TimeUnit.SECONDS.sleep(waitTim);
                    } catch (Exception e) {
                        if (!clientFactoryPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                }

            }
        });

    }

    private void stopApmMsgService(){

        // stop thread
        clientFactoryPoolStoped = true;
        clientFactoryThreadPool.shutdownNow();

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
