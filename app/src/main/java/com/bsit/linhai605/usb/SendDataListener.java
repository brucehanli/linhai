package com.bsit.linhai605.usb;

public interface SendDataListener {
    /**
     * 发送的字节转换为16进制的String
     * @param send
     */
    void onSend(String send);

    /**
     * 接收的字节转换为16进制的String
     * @param rcv
     */
    void onReceive(String rcv);
}
