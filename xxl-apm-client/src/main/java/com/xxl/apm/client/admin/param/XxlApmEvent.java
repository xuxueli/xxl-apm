package com.xxl.apm.client.admin.param;

/**
 * event msg, like "pv、uv、qps、suc rate"
 *
 * @author xuxueli 2018-12-29 16:55:14
 */
public class XxlApmEvent extends XxlApmMsg {

    private String type;
    private int status;
    private String param;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

}