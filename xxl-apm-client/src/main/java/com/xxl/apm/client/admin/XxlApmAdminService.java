package com.xxl.apm.client.admin;

import com.xxl.apm.client.message.XxlApmMsg;
import com.xxl.apm.client.admin.model.XxlApmResult;

import java.util.List;

/**
 * @author xuxueli 2018-12-29 17:16:33
 */
public interface XxlApmAdminService {

    public XxlApmResult report(List<XxlApmMsg> msgList);

}
