package com.xxl.apm.client.admin;

import com.xxl.apm.client.admin.param.XxlApmMsg;
import com.xxl.apm.client.admin.param.XxlApmMsgResult;

import java.util.List;

/**
 * @author xuxueli 2018-12-29 17:16:33
 */
public interface ApmAdminService {

    public XxlApmMsgResult report(List<XxlApmMsg> msgList);

}
