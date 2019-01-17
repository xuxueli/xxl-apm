package com.xxl.apm.admin.conf;

import com.xxl.apm.admin.core.model.XxlCommonRegistryData;
import com.xxl.apm.admin.service.impl.XxlCommonRegistryServiceImpl;
import com.xxl.apm.client.admin.XxlApmMsgService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import com.xxl.apm.client.message.impl.XxlApmMetric;
import com.xxl.apm.client.message.impl.XxlApmTransaction;
import com.xxl.apm.client.util.FileUtil;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
    @Value("${xxl-apm.msglog.path}")
    private String msglogpath;


    // ---------------------- start stop ----------------------

    @Override
    public void afterPropertiesSet() throws Exception {
        startServer();

        startApmThread();
    }

    @Override
    public void destroy() throws Exception {
        stopServer();

        stopApmThread();
    }


    // ---------------------- apm server ----------------------

    private XxlRpcProviderFactory providerFactory;
    private Serializer serializer;

    private void startServer() throws Exception {

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
        serializer = Serializer.SerializeEnum.HESSIAN.getSerializer();

        // add server
        providerFactory.addService(XxlApmMsgService.class.getName(), null, this);

        // start server
        providerFactory.start();
    }

    private void stopServer() throws Exception {
        // stop server
        if (providerFactory != null) {
            providerFactory.stop();
        }
    }


    // ---------------------- apm server thread ----------------------

    private ExecutorService innerThreadPool = Executors.newCachedThreadPool();
    public volatile boolean innerThreadPoolStoped = false;

    private LinkedBlockingQueue<XxlApmMsg> newMessageQueue = new LinkedBlockingQueue<>();
    private int newMessageQueueMax = 100000;

    private volatile File msgFileDir = null;
    private Object msgFileDirLock = new Object();


    // apm msg process thread, report-fail or queue-max, write msg-file
    private void startApmThread(){

        for (int i = 0; i < 10; i++) {
            innerThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    while (!innerThreadPoolStoped) {
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

                                    // process
                                    boolean ret = processMsg(messageList);
                                    if (ret) {
                                        messageList.clear();
                                    }
                                }

                            }
                        } catch (Exception e) {
                            if (!innerThreadPoolStoped) {
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

                        // process
                        boolean ret = processMsg(messageList);
                        if (!ret) {

                            // app stop, write msg-file
                            writeMsgFile(messageList);
                            messageList.clear();
                        }
                    }

                }
            });
        }

        // apm msg-file retry process thread, cycle retry
        innerThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                while (!innerThreadPoolStoped) {

                    int waitTim = 5;
                    try {
                        File msglogpathDir = new File(msglogpath);
                        if (msglogpathDir.list()!=null && msglogpathDir.list().length>0) {
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
                                    if (fileItem.getName().startsWith(XxlApmEvent.class.getSimpleName())) {
                                        msgType = XxlApmEvent.class;
                                    } else if (fileItem.getName().startsWith(XxlApmTransaction.class.getSimpleName())) {
                                        msgType = XxlApmTransaction.class;
                                    } else if (fileItem.getName().startsWith(XxlApmMetric.class.getSimpleName())) {
                                        msgType = XxlApmMetric.class;
                                    } else if (fileItem.getName().startsWith(XxlApmHeartbeat.class.getSimpleName())) {
                                        msgType = XxlApmHeartbeat.class;
                                    } else {
                                        fileItem.delete();
                                        continue;
                                    }

                                    try {

                                        // read msg-file
                                        byte[] serialize_data = FileUtil.readFileContent(fileItem);
                                        List<XxlApmMsg> messageList = (List<XxlApmMsg>) serializer.deserialize(serialize_data, msgType);

                                        // retry process
                                        boolean ret = processMsg(messageList);

                                        // delete
                                        if (ret) {
                                            fileItem.delete();
                                        }
                                    } catch (Exception e) {
                                        if (!innerThreadPoolStoped) {
                                            logger.error(e.getMessage(), e);
                                        }
                                    }

                                }

                            }

                        } else {
                            waitTim = (waitTim+5<=60)?(waitTim+5):60;
                        }

                    } catch (Exception e) {
                        if (!innerThreadPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }

                    }

                    // wait
                    try {
                        TimeUnit.SECONDS.sleep(waitTim);
                    } catch (Exception e) {
                        if (!innerThreadPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                }

            }
        });

    }
    private void stopApmThread(){
        // stop thread
        innerThreadPoolStoped = true;
        innerThreadPool.shutdownNow();
    }


    private boolean processMsg(List<XxlApmMsg> messageList){

        // todo, process msg
        System.out.println(BasicJson.toJson(messageList));

        return true;
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
                // {msglogpath}/{timestamp}/xxxxxx
                msgFileDir = new File(msglogpath, String.valueOf(System.currentTimeMillis()));
                msgFileDir.mkdirs();
            }
        }

        // write msg-file
        writeMsgList(eventList,  XxlApmEvent.class.getSimpleName(), msgFileDir);
        writeMsgList(transactionList, XxlApmTransaction.class.getSimpleName(), msgFileDir);
        writeMsgList(metricList, XxlApmMetric.class.getSimpleName(), msgFileDir);
        writeMsgList(heartbeatList, XxlApmHeartbeat.class.getSimpleName(), msgFileDir);

        return true;
    }


    private void writeMsgList(List<? extends XxlApmMsg> msgList, String filePrefix, File msgFileDir ){
        if (msgList == null) {
            return;
        }

        String msgListFileName = filePrefix.concat("-").concat(msgList.get(0).getMsgId());
        File msgFile = new File(msgFileDir, msgListFileName);

        byte[] serialize_data = serializer.serialize(msgList);
        FileUtil.writeFileContent(msgFile, serialize_data);
    }


    // ---------------------- service ----------------------

    @Override
    public boolean beat() {
        return true;
    }

    @Override
    public boolean report(List<XxlApmMsg> msgList) {
        newMessageQueue.addAll(msgList);
        return true;
    }

}
