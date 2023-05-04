package com.chatgpt.embeddings.vo;

import java.io.Serializable;

/**
 * 对外返回的实体对象
 * @author Zero
 */
public class ReplyMsg implements Serializable {

    private static final long serialVersionUID = -199982507829161706L;

    public static final Integer REPLY_STATUS_SUCCESS = 200;
    public static final Integer REPLY_STATUS_FAIL = 500;

    private Integer status;

    private String msg;

    private Object obj;

    public ReplyMsg() {

    }

    public ReplyMsg(Integer status, String msg, Object obj) {

        this.status = status;
        this.msg = msg;
        this.obj = obj;
    }

    public ReplyMsg(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    /**
     * 返回成功
     */
    public static ReplyMsg retSuccess(Object data) {
        return new ReplyMsg(REPLY_STATUS_SUCCESS, "success", data);
    }

    public static ReplyMsg retSuccessMsg(String msg) {
        return new ReplyMsg(REPLY_STATUS_SUCCESS, msg, null);
    }

    /**
     * 返回失败
     */
    public static ReplyMsg retError(Object data) {
        return new ReplyMsg(REPLY_STATUS_FAIL, "error", data);
    }

    public static ReplyMsg retErrorMsg(String msg) {
        return new ReplyMsg(REPLY_STATUS_FAIL, msg, null);
    }

    /**
     * 返回失败-自定义message
     */
    public static ReplyMsg retError(String message, Object data) {

        return new ReplyMsg(REPLY_STATUS_FAIL, message, data);
    }

    /**
     * 返回成功-自定义message
     */
    public static ReplyMsg retSuccess(String message, Object data) {

        return new ReplyMsg(REPLY_STATUS_SUCCESS, message, data);
    }

    public Integer getStatus() {

        return status;
    }

    public void setStatus(Integer status) {

        this.status = status;
    }

    public String getMsg() {

        return msg;
    }

    public void setMsg(String msg) {

        this.msg = msg;
    }

    public Object getObj() {

        return obj;
    }

    public void setObj(Object obj) {

        this.obj = obj;
    }

}
