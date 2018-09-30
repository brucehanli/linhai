package com.bsit.linhai605.model;

/**
 * 蓝牙发送指令类
 */
public class CmdInfo<T> {
    private int code;//指令编码
    private T obj;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}
