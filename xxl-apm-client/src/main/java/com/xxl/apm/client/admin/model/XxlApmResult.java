package com.xxl.apm.client.admin.model;

import java.io.Serializable;

/**
 * @author xuxueli 2018-12-29 17:15:40
 */
public class XxlApmResult implements Serializable {
    private static final long serialVersionUID = 42L;

    private boolean code;
    private String msg;

    public XxlApmResult() {
        this.code = true;
    }
    public XxlApmResult(boolean code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public boolean isCode() {
        return code;
    }

    public void setCode(boolean code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
