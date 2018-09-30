package com.bsit.linhai605.usb;

public interface SendDataListener {
    int TYPE_CARD_AUTO_REPORT = 0;
    int TYPE_CARD_RESPONSE = 1;
    /**
     * 发送的字节转换为16进制的String
     * @param send
     */
    void onSend(String send);

    /**
     * 接收的字节转换为16进制的String
     * @param what type
     * @param rcv
     */
    void onReceive(int what, String rcv);
}
