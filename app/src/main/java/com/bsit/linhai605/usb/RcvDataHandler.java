package com.bsit.linhai605.usb;

import android.os.Handler;
import android.os.Message;

/**
 * 接收底板数据，消息处理
 */
public class RcvDataHandler extends Handler {
    public static final int NOTICE_DATE = 0;
    private NoticeDataListener mListener;
    private static RcvDataHandler mInstance;

    private RcvDataHandler() {
    }

    public static RcvDataHandler getInstance() {
        if (mInstance == null) {
            mInstance = new RcvDataHandler();
        }
        return mInstance;
    }

    public void setNoticeDateListener(NoticeDataListener listener) {
        mListener = listener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case NOTICE_DATE:
                noticeDate((byte[]) msg.obj);
                break;
            default:
                break;
        }
    }

    private void noticeDate(byte[] data) {
        mListener.receive(data);
    }
}
