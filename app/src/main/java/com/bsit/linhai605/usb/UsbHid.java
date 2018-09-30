package com.bsit.linhai605.usb;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.bsit.linhai605.utils.ByteUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shengbing on 2016/7/22.
 */
public class UsbHid {
    private static UsbHid usbHid = null;
    private static final String TAG = "UsbHid";

    private Context context;
    private UsbManager myUsbManager;
    private UsbDevice myUsbDevice;
    private UsbInterface myInterface;
    private UsbDeviceConnection myDeviceConnection;

    private final int VendorID = 49745;  //bsit P3DP 读卡器 VID and PID
    private final int ProductID = 7169;

    private UsbEndpoint epOut;
    private UsbEndpoint epIn;
    private boolean forceClaim = true;
    private static int TIMEOUT = 300;
    private List<UsbDevice> usbDeviceList;

    private PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private UsbHid(Context context) {
        this.context = context;
        mPermissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        myUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        enumerateDevice();
        Log.d(TAG, "UsbHid init");
    }

    public static UsbHid getInstance(Context context) {
        if (usbHid == null) {
            usbHid = new UsbHid(context);
        }
        return usbHid;
    }

    /**
     * 查看是否有对接读卡器
     */
    public boolean getUsbDevice() {
        enumerateDevice();
        return myUsbDevice != null;
    }

    /**
     * 连接设备
     */
    public void ConnectUsbHid() {
        findInterface();
        openDevice();
        assignEndpoint();
        claimInterface();
    }


    public boolean claimInterface() {
        if (myDeviceConnection != null) {
            return myDeviceConnection.claimInterface(myInterface, forceClaim);
        } else {
            return false;
        }
    }

    public boolean isConnect() {
        return myDeviceConnection != null;
    }

    /**
     * 分配端点，IN | OUT，即输入输出；此处我直接用1为OUT端点，0为IN，当然你也可以通过判断
     */
    public void assignEndpoint() {
        int endpoint_count = myInterface.getEndpointCount();
        Log.w(TAG, "\ngetEndpointCount = " + endpoint_count);
        if (endpoint_count == 2) {
            Log.w(TAG, "\nAssign Endpoint in and out");
            epIn = myInterface.getEndpoint(0);
            epOut = myInterface.getEndpoint(1);
        } else {
            Log.w(TAG, "\nEndpoint count != 2 !!");
        }
    }

    /**
     * 打开设备
     */
    public void openDevice() {
        if (myInterface != null) {
            UsbDeviceConnection conn = null;
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限，可以查阅相关资料
            if (myUsbManager.hasPermission(myUsbDevice)) {
                conn = myUsbManager.openDevice(myUsbDevice);
            } else {
                myUsbManager.requestPermission(myUsbDevice, mPermissionIntent);
            }

            if (conn == null) {
                Log.w(TAG, "\nconn == null !!");
                return;
            }

            if (conn.claimInterface(myInterface, true)) {
                myDeviceConnection = conn; // 到此你的android设备已经连上HID设备
                Log.w(TAG, "\n打开设备成功");
                return;
            } else {
                conn.close();
                Log.w(TAG, "\n打开设备失败");
            }
        } else {
            Log.w(TAG, "\nmyInterface == null");
        }
    }

    /**
     * 找设备接口
     */
    public void findInterface() {
        if (myUsbDevice != null) {
            Log.w(TAG, "\ninterfaceCounts : " + myUsbDevice.getInterfaceCount());
            for (int i = 0; i < myUsbDevice.getInterfaceCount(); i++) {
                UsbInterface intf = myUsbDevice.getInterface(i);
                Log.w(TAG, "\nintf.getInterfaceClass() = " + intf.getInterfaceClass());
                Log.w(TAG, "\nintf.getInterfaceSubclass() = " + intf.getInterfaceSubclass());
                Log.w(TAG, "\nintf.getInterfaceProtocol() = " + intf.getInterfaceProtocol());
                if (intf.getInterfaceClass() == 3
                        && intf.getInterfaceSubclass() == 0
                        && intf.getInterfaceProtocol() == 0) {
                    myInterface = intf;
                    Log.w(TAG, "\n找到我的设备接口");
                }
                break;
            }
        }
    }

    /**
     * 获取usb设备列表
     *
     * @return
     */
    public List<UsbDevice> getUsbDeviceList() {
        enumerateDevice();
        return usbDeviceList;
    }

    /**
     * 枚举设备
     */
    public void enumerateDevice() {
        if (myUsbManager == null) {
            Log.w(TAG, "\n设备管理器初始化失败");
            return;
        }
        HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();

        if (!deviceList.isEmpty()) { // deviceList不为空
            usbDeviceList = new ArrayList<>();
            for (UsbDevice device : deviceList.values()) {
                // 输出设备信息
                Log.w(TAG, "\nDeviceInfo: " + device.getVendorId() + " , "
                        + device.getProductId());
                // 枚举到设备
                if (device.getVendorId() == VendorID
                        && device.getProductId() == ProductID) {
                    usbDeviceList.add(device);
                    myUsbDevice = device;
                    Log.w(TAG, "\n枚举设备成功");
                }
            }
        }
    }

    /**
     * 发送指令
     *
     * @param data
     */
    public void SendData(byte[] data) {
        Log.d(TAG, "Send Data" + ByteUtil.byte2HexStr(data));
        if (myDeviceConnection != null) {
            int sendReturn = myDeviceConnection.bulkTransfer(epOut, data, 64, TIMEOUT);
            if (sendReturn <= 0) {
                reConnectDevice();
            }
        }
    }

    /**
     * 接收指令
     *
     * @return
     */
    public byte[] RecData() {
        byte[] data = new byte[64];
        if (myDeviceConnection != null) {
            myDeviceConnection.bulkTransfer(epIn, data, 64, TIMEOUT);
        }
        return data;
    }

    private void reConnectDevice() {
        closeDevice();
        enumerateDevice();
        ConnectUsbHid();
    }

    /**
     * 关闭设备
     */
    public void closeDevice() {
        if (myDeviceConnection != null) {
            myDeviceConnection.releaseInterface(myInterface);
            myDeviceConnection.close();
        }
        myDeviceConnection = null;
        myInterface = null;
        myUsbDevice = null;
    }
}

