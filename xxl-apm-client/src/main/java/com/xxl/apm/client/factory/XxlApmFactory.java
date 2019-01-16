package com.xxl.apm.client.factory;

import com.xxl.apm.client.XxlApm;
import com.xxl.apm.client.admin.XxlApmMsgService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import com.xxl.apm.client.message.impl.XxlApmMetric;
import com.xxl.apm.client.message.impl.XxlApmTransaction;
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
    private int newMessageQueueMax = 100000;

    private volatile File msgFileDir = null;
    private Object msgFileDirLock = new Object();

    /**
     * async report msg
     *
     * @param msgList
     * @return
     */
    public boolean report(List<XxlApmMsg> msgList) {
        newMessageQueue.addAll(msgList);
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

                                if (newMessageQueue.size() < newMessageQueueMax) {
                                    // report
                                    boolean ret = xxlApmMsgService.report(messageList);
                                    if (ret) {
                                        messageList.clear();
                                    }
                                }

                            }
                        } catch (Exception e) {
                            if (!clientFactoryPoolStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        } finally {

                            // report-fail or queue-max, write msg-file
                            if (messageList!=null && messageList.size()>0) {

                                writeMsgFile(messageList);
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

                            // app stop, write msg-file
                            writeMsgFile(messageList);
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
                        boolean beatResult = xxlApmMsgService.beat();

                        File msglogpathDir = new File(msglogpath);
                        if (beatResult && msglogpathDir.list()!=null && msglogpathDir.list().length>0) {
                            waitTim = 5;

                            // {msglogpath}/{yyyy-MM-dd}_xxx
                            for (File msgFileDir : msglogpathDir.listFiles()) {

                                // clean invalid file
                                if (msgFileDir.isFile()) {
                                    msgFileDir.delete();
                                    continue;
                                }
                                // clean empty dir
                                if (!(msgFileDir.list()!=null && msgFileDir.list().length>0)) {
                                    msgFileDir.delete();
                                    continue;
                                }

                                // {msglogpath}/{yyyy-MM-dd}_xxx/xxxxxx
                                for (File fileItem: msgFileDir.listFiles()) {

                                    Class<? extends XxlApmMsg> msgType = null;
                                    if (fileItem.getName().startsWith("XxlApmEvent")) {
                                        msgType = XxlApmEvent.class;
                                    } else if (fileItem.getName().startsWith("XxlApmTransaction")) {
                                        msgType = XxlApmTransaction.class;
                                    } else if (fileItem.getName().startsWith("XxlApmMetric")) {
                                        msgType = XxlApmMetric.class;
                                    } else if (fileItem.getName().startsWith("XxlApmHeartbeat")) {
                                        msgType = XxlApmHeartbeat.class;
                                    } else {
                                        fileItem.delete();
                                        continue;
                                    }

                                    try {

                                        // read msg-file
                                        String msgListJson = FileUtil.readFileContent(fileItem);
                                        List<XxlApmMsg> messageList = (List<XxlApmMsg>) BasicJson.parseList(msgListJson, msgType);

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


    // msg-file
    private boolean writeMsgFile(List<XxlApmMsg> msgList){

        // dispatch msg
        List<XxlApmEvent> eventList = null;
        List<XxlApmTransaction> transactionList = null;
        List<XxlApmMetric> metricList = null;
        List<XxlApmHeartbeat> heartbeatList = null;

        for (XxlApmMsg apmMsg: msgList) {
            if (apmMsg instanceof XxlApmEvent) {
                if (eventList == null) {
                    eventList = new ArrayList<>();
                }
                eventList.add((XxlApmEvent) apmMsg);
            } else if (apmMsg instanceof XxlApmTransaction) {
                if (transactionList == null) {
                    transactionList = new ArrayList<>();
                }
                transactionList.add((XxlApmTransaction) apmMsg);
            } else if (apmMsg instanceof XxlApmMetric) {
                if (metricList == null) {
                    metricList = new ArrayList<>();
                }
                metricList.add((XxlApmMetric) apmMsg);
            } else if (apmMsg instanceof XxlApmHeartbeat) {
                if (heartbeatList == null) {
                    heartbeatList = new ArrayList<>();
                }
                heartbeatList.add((XxlApmHeartbeat) apmMsg);
            }
        }

        // make msg-file dir
        if (msgFileDir==null || msgFileDir.list().length>10000) {
            synchronized (msgFileDirLock) {
                // {msglogpath}/{yyyy-MM-dd}_xxx
                msgFileDir = new File(msglogpath, String.valueOf(System.currentTimeMillis()));
                msgFileDir.mkdirs();
            }
        }

        // write msg-file
        writeMsgList(eventList,  "XxlApmEvent", msgFileDir);
        writeMsgList(transactionList, "XxlApmTransaction", msgFileDir);
        writeMsgList(metricList, "XxlApmMetric", msgFileDir);
        writeMsgList(heartbeatList, "XxlApmHeartbeat", msgFileDir);

        return true;
    }


    private void writeMsgList(List<? extends XxlApmMsg> msgList, String filePrefix, File msgFileDir ){
        if (msgList == null) {
            return;
        }

        String msgListFileName = filePrefix.concat("-").concat(msgList.get(0).getMsgId());
        File msgFile = new File(msgFileDir, msgListFileName);

        String eventListJson = BasicJson.toJson(msgList);
        FileUtil.writeFileContent(msgFile, eventListJson);
    }

}
