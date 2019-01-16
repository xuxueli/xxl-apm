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
        startApmMsgService(adminAddress, accessToken, msglogpathDir);

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

    private File msglogpathDir;
    private long maxFileLengh_32M_byte = 32 * 1024 * 1024;

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
    private void startApmMsgService(final String adminAddress, final String accessToken, final File msglogpath){
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


        // msg logpathDir
        this.msglogpathDir = msglogpath;

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
                                int drainToNum = newMessageQueue.drainTo(otherMessageList, 400);
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
                        if (beatResult && msglogpathDir.listFiles()!=null && msglogpathDir.listFiles().length>0) {
                            waitTim = 5;
                            for (File fileItem : msglogpathDir.listFiles()) {

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

        // dispatch msg-file
        File file_XxlApmEvent = null;
        File file_XxlApmTransaction = null;
        File file_XxlApmMetric = null;
        File file_XxlApmHeartbeat = null;

        if (msglogpathDir.listFiles()!=null && msglogpathDir.listFiles().length>0) {
            for (File file : msglogpathDir.listFiles()) {
                if (file.length() > maxFileLengh_32M_byte) {
                    continue;
                }
                if (file_XxlApmEvent==null && file.getName().startsWith("XxlApmEvent")) {
                    file_XxlApmEvent = file;
                } else if (file_XxlApmTransaction==null && file.getName().startsWith("XxlApmTransaction")) {
                    file_XxlApmTransaction = file;
                } else if (file_XxlApmMetric==null && file.getName().startsWith("XxlApmMetric")) {
                    file_XxlApmMetric = file;
                } else if (file_XxlApmHeartbeat==null && file.getName().startsWith("XxlApmHeartbeat")) {
                    file_XxlApmHeartbeat = file;
                }
            }
        }

        // write msg-file
        writeMsgList(eventList, file_XxlApmEvent, "XxlApmEvent");
        writeMsgList(transactionList, file_XxlApmTransaction, "XxlApmTransaction");
        writeMsgList(metricList, file_XxlApmMetric, "XxlApmMetric");
        writeMsgList(heartbeatList, file_XxlApmHeartbeat, "XxlApmHeartbeat");

        return true;
    }

    private void writeMsgList(List<? extends XxlApmMsg> msgList, File msgFile, String filePrefix){
        if (msgList == null) {
            return;
        }

        if (msgFile == null) {
            String msgListFileName = msglogpath.concat(File.separator)
                    .concat(filePrefix).concat("-").concat(msgList.get(0).getMsgId());
            msgFile = new File(msgListFileName);
        }
        String eventListJson = BasicJson.toJson(msgList);
        FileUtil.writeFileContent(msgFile, eventListJson);
    }


}
