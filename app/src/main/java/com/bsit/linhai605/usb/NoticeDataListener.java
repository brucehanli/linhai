package com.bsit.linhai605.usb;

public interface NoticeDataListener {
    /**
     * 底板返回的字节数组
     * @param response
     */
    void receive(byte[] response);
}
