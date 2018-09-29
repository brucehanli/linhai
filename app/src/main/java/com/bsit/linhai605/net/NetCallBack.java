package com.bsit.linhai605.net;

/**
 * Created by DELL on 2017/12/19.
 */

public interface NetCallBack {
    void successCallBack(String successMsg);

    void failedCallBack(String failedMsg, int code);
}
