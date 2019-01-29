package com.xxl.apm.client.factory;

import com.xxl.apm.client.XxlApm;
import com.xxl.apm.client.admin.XxlApmMsgService;
import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.message.impl.XxlApmEvent;
import com.xxl.apm.client.message.impl.XxlApmHeartbeat;
import com.xxl.apm.client.message.impl.XxlApmTransaction;
import com.xxl.apm.client.util.FileUtil;
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
import java.util.*;
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

    public String getAppname() {
        return appname;
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

        logger.info(">>>>>>>>>>> xxl-apm start.");
    }

    public void stop(){
        // stop XxlApmMsgService
        stopApmMsgService();

        logger.info(">>>>>>>>>>> xxl-apm stop.");
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
    private Serializer serializer;

    private ExecutorService innerThreadPool = Executors.newCachedThreadPool();
    public volatile boolean innerThreadPoolStoped = false;

    private LinkedBlockingQueue<XxlApmMsg> newMessageQueue = new LinkedBlockingQueue<>();
    private int newMessageQueueMax = 50000;

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
        serializer = Serializer.SerializeEnum.HESSIAN.getSerializer();


        // apm msg remote report thread, report-fail or queue-max, write msg-file
        for (int i = 0; i < 5; i++) {
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

                                // attempt to report mult msg
                                List<XxlApmMsg> otherMessageList = new ArrayList<>();
                                int drainToNum = newMessageQueue.drainTo(otherMessageList, 100);
                                if (drainToNum > 0) {
                                    messageList.addAll(otherMessageList);
                                }

                                // msg too small, just wait 1s, avoid report too quick
                                if (otherMessageList.size() < 50) {
                                    TimeUnit.SECONDS.sleep(1);

                                    drainToNum = newMessageQueue.drainTo(otherMessageList, 50);
                                    if (drainToNum > 0) {
                                        messageList.addAll(otherMessageList);
                                    }
                                }

                                // queue small to remote support；queue large, quick move to msg-file
                                if (newMessageQueue.size() < newMessageQueueMax) {
                                    // report
                                    boolean ret = xxlApmMsgService.report(messageList);
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

        // apm msg-file retry remote report thread, cycle retry
        innerThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                while (!innerThreadPoolStoped) {

                    int waitTim = 3;
                    try {
                        boolean beatResult = xxlApmMsgService.beat();

                        File msglogpathDir = new File(msglogpath);
                        if (beatResult && msglogpathDir.list()!=null && msglogpathDir.list().length>0) {
                            waitTim = 3;

                            // {msglogpath}/{timestamp}
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

                                // {msglogpath}/{timestamp}/xxxxxx
                                for (File fileItem: msgFileDir.listFiles()) {

                                    Class<? extends XxlApmMsg> msgType = null;
                                    if (fileItem.getName().startsWith(XxlApmEvent.class.getSimpleName())) {
                                        msgType = XxlApmEvent.class;
                                    } else if (fileItem.getName().startsWith(XxlApmTransaction.class.getSimpleName())) {
                                        msgType = XxlApmTransaction.class;
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

                                        // retry report
                                        boolean ret = xxlApmMsgService.report(messageList);

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


        // heartbeat thread, cycle report for 1min ("heartbeat" for 1min, other "event、transaction" for real-time)
        innerThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                // align to minute
                try {
                    long sleepSecond = 0;
                    Calendar nextMin = Calendar.getInstance();
                    nextMin.add(Calendar.MINUTE, 1);
                    nextMin.set(Calendar.SECOND, 0);
                    nextMin.set(Calendar.MILLISECOND, 0);
                    sleepSecond = (nextMin.getTime().getTime() - System.currentTimeMillis())/1000;
                    if (sleepSecond>0 && sleepSecond<60) {
                        TimeUnit.SECONDS.sleep(sleepSecond);
                    }
                } catch (Exception e) {
                    if (!innerThreadPoolStoped) {
                        logger.error(e.getMessage(), e);
                    }
                }

                while (!innerThreadPoolStoped) {
                    // heartbeat report
                    try {
                        XxlApm.report(new XxlApmHeartbeat());
                    } catch (Exception e) {
                        if (!innerThreadPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    // wait
                    try {
                        TimeUnit.MINUTES.sleep(1);
                    } catch (Exception e) {
                        if (!innerThreadPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

    }

    private void stopApmMsgService(){

        // stop thread
        innerThreadPoolStoped = true;
        innerThreadPool.shutdownNow();

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

}
