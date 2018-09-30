package com.bsit.linhai605.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bsit.linhai605.BaseApplication;
import com.bsit.linhai605.R;
import com.bsit.linhai605.bluetooth.BtBase;
import com.bsit.linhai605.bluetooth.BtServer;
import com.bsit.linhai605.bluetooth.QRCodeUtil;
import com.bsit.linhai605.constant.Constant;
import com.bsit.linhai605.model.CmdInfo;
import com.bsit.linhai605.model.CommonBackJson;
import com.bsit.linhai605.model.ConfigueInfo;
import com.bsit.linhai605.model.ConsumeInfo;
import com.bsit.linhai605.utils.SharedUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class DeviceSetActivity extends Activity implements BtBase.Listener {
    private TextView mTips;
    private TextView mLogs;
    private ImageView imageView;
    private BtServer mServer;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set);
        mTips = findViewById(R.id.tv_tips);
        mLogs = findViewById(R.id.tv_log);
        imageView = findViewById(R.id.iv_bt_mac);
        mServer = new BtServer(this);
        bitmap = QRCodeUtil.createQRImage(BluetoothAdapter.getDefaultAdapter().getAddress(), 600, 600);
        imageView.setImageBitmap(bitmap);
        // 检查蓝牙开关
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(this, "本机没有找到蓝牙硬件或驱动", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            if (!adapter.isEnabled()) {
                //直接开启蓝牙
                adapter.enable();
            }
        }

        // Android 6.0动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_COARSE_LOCATION};
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                    break;
                }
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.unListener();
        mServer.close();
    }


    @Override
    public void socketNotify(int state, final Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                mServer.listen();
                msg = "连接断开,正在重新监听...";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:
                msg = String.format("\n%s", obj);
                String receive = String.format("%s", obj);
                try {
                    JSONObject jsonObject = new JSONObject(receive);
                    switch (jsonObject.getInt("code")) {
                        case Constant.CONFIGURATION:
                            Gson gson = new Gson();
                            CmdInfo<ConfigueInfo> cmdInfo = gson.fromJson(receive, new TypeToken<CmdInfo<ConfigueInfo>>() {
                            }.getType());
                            ConfigueInfo configueInfo = cmdInfo.getObj();
                            SharedUtils.setTypeCody(this,configueInfo.getTypeCode());
                            SharedUtils.setConditionCode(this,configueInfo.getConditionCode());
                            SharedUtils.setMerchantNo(this,configueInfo.getMerchantNo());
                            SharedUtils.setTermId(this,configueInfo.getTermId());
                            SharedUtils.setCorpId(this,configueInfo.getCorpId());
                            //TODO 处理收到配置信息
                            break;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mLogs.append(msg);
                break;
        }
    }

    public void sendMsg(String msg) {
        if (mServer.isConnected(null)) {
            if (TextUtils.isEmpty(msg))
                BaseApplication.toast("消息不能空", 0);
            else
                mServer.sendMsg(msg);
        } else
            BaseApplication.toast("没有连接", 0);
    }
}