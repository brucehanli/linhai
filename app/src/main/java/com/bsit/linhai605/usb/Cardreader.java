package com.bsit.linhai605.usb;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.bsit.linhai605.utils.ByteUtil;

import java.util.Arrays;

/**
 * Created by shengbing on 2016/7/22.
 */
public class Cardreader {

    private UsbHid usbhid = null;
    private byte bseq = 0;
    private SendDataListener mSendListener;
    private RcvDataHandler ihandler;

    public Cardreader() {
        ihandler = RcvDataHandler.getInstance();
        ihandler.setNoticeDateListener(new NoticeDataListener() {
            @Override
            public void receive(byte[] response) {
                mSendListener.onReceive(ByteUtil.byte2HexStr(response));
            }
        });
    }

    /**
     * 打开读取节点，该方法要在其他操作之前调用
     *
     * @param context
     */
    public void openreader(Context context) {
        usbhid = UsbHid.getInstance(context);
        if (!isConnect()) {
            if (usbhid.getUsbDevice()) {
                usbhid.ConnectUsbHid();
            }
        }
    }

    public void setSendDataListener(SendDataListener listener) {
        mSendListener = listener;
    }

    public boolean isConnect() {
        return usbhid.isConnect();
    }


    /**
     * 打开读卡器
     * bslot ==0  非接触卡
     * bslot ==1   PSAM1
     * bslot ===2  psam2
     * bslot ==0x0e 接触卡
     */
    public byte[] card_poweron(int bslot) {
        Log.e("SEND", "上电复位");
        byte[] src = new byte[512];
        src[0] = 0x62;
        src[1] = (byte) 0 & 0xff;
        src[2] = (byte) (0 >> 8) & 0xff;
        src[3] = (byte) (0 >> 16) & 0xff;
        src[4] = (byte) (0 >> 24) & 0xff;
        src[5] = (byte) bslot;
        src[6] = bseq++;
        src[7] = (byte) 0;
        src[8] = (byte) 0;
        src[9] = 0;

        int datalen = 0 + 10;
        byte[] datatosend = new byte[64];
        if (datalen > 0) {
            System.arraycopy(src, 0, datatosend, 0, datalen);
//            Log.e("SEND", "card_poweron");
            usbhid.SendData(datatosend);
        }
        byte[] recvdata = null;
        do {
            recvdata = usbhid.RecData();
        }
        while (recvdata[0] != -128);
        if (recvdata == null) {
            return null;
        }
        int recvlen = recvdata[1] + recvdata[2] * 256 + recvdata[3] * 256 * 256 + recvdata[4] * 256 * 256 * 256;
        byte[] atr = null;
        recvlen += 10;
        if (recvlen < 64) {
            if (recvlen > 10) {
                atr = new byte[recvlen - 10];
                System.arraycopy(recvdata, 10, atr, 0, recvlen - 10);
            }
        }
//        Log.e("RCV_card_poweron", ByteUtil.byte2HexStr(atr));
        return atr;
    }

    /**
     * 发送指令给读卡器
     *
     * @param bslot
     * @param cmdlen
     * @param cmd
     * @return response byte array
     */
    public byte[] sendApdu(int bslot, int cmdlen, byte[] cmd) {
        Log.e("SEND", ByteUtil.byte2HexStr(cmd));
        byte[] src = new byte[512];
        System.arraycopy(cmd, 0, src, 0, cmd.length);
        int offset = 0;
        int datalen = cmdlen;
        byte[] datatosend = new byte[64];
        while (datalen > 0) {
            if (datalen > 64) {
                System.arraycopy(src, offset, datatosend, 0, 64);
                datalen -= 64;
                offset += 64;
            } else {
                System.arraycopy(src, offset, datatosend, 0, datalen);
                offset += datalen;
                datalen = 0;
            }
            Log.i("SEND-FULL", ByteUtil.byte2HexStr(datatosend));
            usbhid.SendData(datatosend);
            mSendListener.onSend(ByteUtil.byte2HexStr(datatosend));
        }
        return rvcResNoDelay();

//        rcvResDelay();
//        return new byte[]{0x0, 0x0};
    }

    /**
     * 发送请求后不延时接收数据
     *
     * @return 底板返回的数组
     */
    private byte[] rvcResNoDelay() {
        synchronized (usbhid) {
            byte[] resp = new byte[512];
            int resplen = 0;
            byte[] recvdata = null;
            int count = 0;
            do {
                if (count > 4) {
                    usbhid.notify();
                    return null;
                }
                recvdata = usbhid.RecData();
                Log.i("RCV-------do-while----", ByteUtil.byte2HexStr(recvdata));
                count++;
            }
            while ((recvdata[0] != -128) && (recvdata[0] != -126));
            if (recvdata == null) {
                return null;
            }
            int recvlen = recvdata[1] + recvdata[2] * 256 + recvdata[3] * 256 * 256 + recvdata[4] * 256 * 256 * 256;
            if (recvlen == 0)
                return null;
            int leftlen = recvlen;
            recvlen += 10;
            if (recvlen < 64) {
                System.arraycopy(recvdata, 10, resp, 0, recvlen - 10);
                leftlen = 0;
                resplen = recvlen - 10;
            } else {
                System.arraycopy(recvdata, 10, resp, 0, 64 - 10);
                leftlen -= (64 - 10);
                resplen = 64 - 10;
            }

            while (leftlen > 0) {
                recvdata = usbhid.RecData();
                if (leftlen > 64) {
                    System.arraycopy(recvdata, 0, resp, resplen, 64);
                    leftlen -= 64;
                    resplen += 64;
                } else {
                    System.arraycopy(recvdata, 0, resp, resplen, leftlen);
                    leftlen -= leftlen;
                    resplen += leftlen;
                }
            }
            byte[] ret_resp = new byte[resplen];
            System.arraycopy(resp, 0, ret_resp, 0, resplen);
            Log.i("RCV-------------", ByteUtil.byte2HexStr(ret_resp));
            mSendListener.onReceive(ByteUtil.byte2HexStr(ret_resp));
            return ret_resp;
        }
    }

    private byte[] sendPDU(byte[] sendBytes) {
        return sendApdu(0, sendBytes.length, sendBytes);
    }

    /**
     * 接收底板主动发送的数据
     */
    public void listenRcvData() {
        new Thread() {
            @Override
            public void run() {
//                byte[] resp = new byte[512];
                recycleRcvResp();
            }
        }.start();
    }

    /**
     * 监听底板发送的数据
     *
     * @return
     */
    public byte[] recycleRcvResp() {
        synchronized (usbhid) {
            byte[] recvdata;
            while (true) {
                recvdata = usbhid.RecData();
                if (recvdata[0] == -128) {
                    try {
                        int recvlen = recvdata[1] + recvdata[2] * 256 + recvdata[3] * 256 * 256 + recvdata[4] * 256 * 256 * 256;
                        byte[] ret_resp = new byte[recvlen];
                        System.arraycopy(recvdata, 10, ret_resp, 0, recvlen);
                        Log.i("&&&&&&&&&&&&&&&", ByteUtil.byte2HexStr(ret_resp));
                        Message message = ihandler.obtainMessage();
                        message.what = RcvDataHandler.NOTICE_DATE;
                        message.obj = ret_resp;
                        ihandler.sendMessage(message);
                        usbhid.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    /**
     * 关闭读卡器
     *
     * @param bslot card type
     * @return
     */
    public int card_poweroff(int bslot) {
        byte[] src = new byte[512];
        src[0] = 0x63;
        src[1] = (byte) 0 & 0xff;
        src[2] = (byte) (0 >> 8) & 0xff;
        src[3] = (byte) (0 >> 16) & 0xff;
        src[4] = (byte) (0 >> 24) & 0xff;
        src[5] = (byte) bslot;
        src[6] = bseq++;
        src[7] = (byte) 0;
        src[8] = (byte) 0;
        src[9] = 0;

        int datalen = 0 + 10;
        byte[] datatosend = new byte[64];
        System.arraycopy(src, 0, datatosend, 0, datalen);
        while (datalen > 0) {
            if (datalen > 64)
                datalen -= 64;
            else
                datalen = 0;
            usbhid.SendData(datatosend);
        }
        byte[] recvdata = null;
        recvdata = usbhid.RecData();
        return 0;
    }

    public int closereader() {
        usbhid.closeDevice();
        return 0;
    }

    /**
     * 获取设备编号
     *
     * @return 固定数据格式：MessageType为0x82，SlotNum为0xFE, SeqNum为0x01。
     * 举例：
     * 82 0E 00 00 00 FE 01 00 00 00 05 E1 FF 30 37 34 50 4E 51 25 69 28 90 00
     * 除固定数据格式外，0E代表数据域长度为14个字节，数据域90 00代表读取设备编号成功，
     * 05 E1 FF 30 37 34 50 4E 51 25 69 28为SN编号，
     * 编码方式为ASCII
     */
    public byte[] getDeviceSN() {
        /**
         * RDA下发 获取SN指令
         固定数据格式： MessageType为0x6C， MsgLen 为0x05 0x00 0x00 0x00，SlotNum为0xFE, SeqNum为0x00,
         Data为FF 19 00 10 00。
         举例：
         6C 05 00 00 00 FE 00 00 00 00 FF 19 00 10 00
         */
        byte[] getSnBytes = {0x6C, 0x05, 0x00, 0x00, 0x00, (byte) 0xFE, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, 0x19, 0x00, 0x10, 0x00};
        byte[] rcvSnBytes = sendApdu(0, getSnBytes.length, getSnBytes);
        if ((rcvSnBytes != null) && (rcvSnBytes[rcvSnBytes.length - 2] == -112) && (rcvSnBytes[rcvSnBytes.length - 1] == 0)) { //数据域90 00代表读取设备编号成功
            return Arrays.copyOf(rcvSnBytes, rcvSnBytes.length - 2);
        }
        return null;
    }

    /**
     * 获取APP 版本号
     *
     * @return
     */
    public byte[] getAppVersion() {
        byte[] getAppVer = {0x6C, 0x05, 0x00, 0x00, 0x00, (byte) 0xFD, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, 0x19, 0x00, 0x20, 0x00};
        byte[] rcvAppBytes = sendApdu(0, getAppVer.length, getAppVer);
        if ((rcvAppBytes != null) && (rcvAppBytes[rcvAppBytes.length - 2] == -112) && (rcvAppBytes[rcvAppBytes.length - 1] == 0)) { //数据域90 00代表获取APP版本号成功
            return Arrays.copyOf(rcvAppBytes, rcvAppBytes.length - 2);
        }
        return null;
    }

    /**
     * RDA复位卡信息
     *
     * @return
     */
    public boolean resetCardInfo() {
        byte[] resetCardBytes = {0x62, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00};
        byte[] rcvResetBytes = sendPDU(resetCardBytes);
        if (rcvResetBytes != null) {
            int length = rcvResetBytes.length;
            if ((rcvResetBytes[length - 2] == -112) && (rcvResetBytes[length - 1] == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * RDA下发卡操作通信指令
     *
     * @param operatBytes
     * @return
     */
    public boolean operCommInstruct(byte[] operatBytes) {
        int dataLen = operatBytes.length;
        byte[] lenBytes = new byte[4];
        lenBytes[0] = (byte) (dataLen & 0xFF);
        lenBytes[1] = (byte) ((dataLen >> 8) & 0xFF);
        lenBytes[2] = (byte) ((dataLen >> 16) & 0xFF);
        lenBytes[3] = (byte) ((dataLen >> 24) & 0xFF);
        byte type = (byte) 0x6F;
        byte slotNum = 0x01;
        byte seqNum = 0x00;
        byte[] prefixBytes = {type, lenBytes[0], lenBytes[1], lenBytes[2], lenBytes[3], slotNum, seqNum};
        int sendLen = dataLen + 10;
        byte[] sendBytes = new byte[sendLen];
        System.arraycopy(prefixBytes, 0, sendBytes, 0, prefixBytes.length);
        System.arraycopy(operatBytes, 0, sendBytes, prefixBytes.length, operatBytes.length);
        byte[] rcvOperateBytes = sendPDU(sendBytes);
        if ((rcvOperateBytes != null) && (rcvOperateBytes[rcvOperateBytes.length - 2] == (byte) 0x6F) && (rcvOperateBytes[rcvOperateBytes.length - 1] == 0)) {
            return true;
        }
        return false;
    }
}
