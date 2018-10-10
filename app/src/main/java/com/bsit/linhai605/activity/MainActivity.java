package com.bsit.linhai605.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsit.linhai605.BaseApplication;
import com.bsit.linhai605.R;
import com.bsit.linhai605.constant.Constant;
import com.bsit.linhai605.constant.Ip;
import com.bsit.linhai605.model.CommonBackJson;
import com.bsit.linhai605.model.OrderInfo;
import com.bsit.linhai605.model.SaveHeart;
import com.bsit.linhai605.net.NetCallBack;
import com.bsit.linhai605.net.OkHttpHelper;
import com.bsit.linhai605.usb.Cardreader;
import com.bsit.linhai605.usb.KeyMap;
import com.bsit.linhai605.usb.SendDataListener;
import com.bsit.linhai605.utils.ByteUtil;
import com.bsit.linhai605.utils.CommonUtil;
import com.bsit.linhai605.utils.EncryptUtils;
import com.bsit.linhai605.utils.SharedUtils;
import com.bsit.linhai605.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanzhenjie.zbar.camera.CameraPreview;
import com.yanzhenjie.zbar.camera.ScanCallback;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends Activity implements SendDataListener {

    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.img_net)
    ImageView imgNet;
    @BindView(R.id.capture_preview)
    CameraPreview capturePreview;
    @BindView(R.id.tv_main_msg)
    TextView tvMainMsg;
    @BindView(R.id.capture_scan_line)
    ImageView captureScanLine;
    private Cardreader mCardReader;
    private ValueAnimator mScanAnimator;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0://更新时间
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    tvTime.setText(time);
                    sendEmptyMessageDelayed(0, 1000);
                    break;
                case 1:
                    heart();
                    break;
            }
        }
    };
    private ScanCallback resultCallback = new ScanCallback() {
        @Override
        public void onScanResult(String content) {
            Log.e("扫描结果", content);
            qrConsume(content);
            stopScan();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startScanUnKnowPermission();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        capturePreview.setScanCallback(resultCallback);
        initNet();
        heart();
        getUsbDeviceParams();
        if (TextUtils.isEmpty(SharedUtils.getToken(this)))
            login();
        else {
            cardConsume("31700000011333961526");
            cardConsume("31700000010000313306");
        }
    }

    private void initNet() {
        if (CommonUtil.isNetworkConnected(this)) {
            imgNet.setImageResource(R.mipmap.ic_net);
//            new GetTime().execute();
            handler.sendEmptyMessage(0);
        } else {//网络未连接图标
            imgNet.setImageResource(R.mipmap.ic_net);
        }
    }

    public void getUsbDeviceParams() {
        mCardReader = new Cardreader();
        mCardReader.setSendDataListener(this);
        mCardReader.openreader(this);
//        byte[] cardSN = mCardReader.getDeviceSN();
        mCardReader.listenRcvData();
    }

    /**
     * Do not have permission to request for permission and start scanning.
     */
    private void startScanUnKnowPermission() {
        startScanWithPermission();
    }

    /**
     * There is a camera when the direct scan.
     */
    private void startScanWithPermission() {
        if (capturePreview.start()) {
            capturePreview.start();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.camera_failure)
                    .setMessage(R.string.camera_hint)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void login() {
        Map param = new HashMap<>();
        OkHttpHelper.getInstance().post(this, Ip.LOGIN, param, new NetCallBack() {
            @Override
            public void successCallBack(String successMsg) {
                Log.e("LOGIN", successMsg);
                Gson gson = new Gson();
                CommonBackJson<String> commonBackJson = gson.fromJson(successMsg, new TypeToken<CommonBackJson<String>>() {
                }.getType());
                if ("1".equals(commonBackJson.getCode())) {
                    SharedUtils.setToken(MainActivity.this, commonBackJson.getContent());
                } else {
                    ToastUtils.showToast(MainActivity.this, commonBackJson.getMessage());
                }
            }

            @Override
            public void failedCallBack(String failedMsg, int code) {
                Log.e("LOGIN", failedMsg + code);
            }
        });
    }

    /**
     * 二维码消费
     */
    private void qrConsume(final String qrMsg) {
        final String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        final Map param = new HashMap<>();
        String typeCode = SharedUtils.getTypeCody(this);
        String conditionCode = SharedUtils.getConditionCode(this);
        String termId = SharedUtils.getTermId(this);
        String merchantNo = SharedUtils.getMerchantNo(this);
        String corpId = SharedUtils.getCorpId(this);
        param.put("qrMessage", qrMsg);
        param.put("txnAmt", "100");
        param.put("localDate", time.substring(2, 8));
        param.put("localTime", time.substring(8, 14));
        param.put("typeCode", typeCode);
        param.put("conditionCode", conditionCode);
        param.put("consumeType", "L");
        param.put("merchantNo", merchantNo);//商户号
        param.put("termId", termId);
        param.put("deviceId", CommonUtil.getWifiMac(MainActivity.this));
        param.put("corpId", corpId);
        param.put("batchNo", time.substring(0, 8));
        OkHttpHelper.getInstance().post(this, Ip.GET_QR_INFO, param, new NetCallBack() {
            @Override
            public void successCallBack(String successMsg) {
                Log.e("QR", successMsg);
                //TODO 保存消费 Long id, String cardId, String txnAmt, String time, boolean isUpload, boolean isQr
                BaseApplication.getDaoInstant().insert(new OrderInfo(null, qrMsg, (String) param.get("txnAmt"), time, true, true));
            }

            @Override
            public void failedCallBack(String failedMsg, int code) {
                Log.e("QR", failedMsg + code);
                //消费失败保存本地
                BaseApplication.getDaoInstant().insert(new OrderInfo(null, qrMsg, (String) param.get("txnAmt"), time, false, true));
            }
        });

    }

    /**
     * 卡消费
     */
    private void cardConsume(final String cardId) {

        final String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        final Map param = new HashMap<>();
        String typeCode = SharedUtils.getTypeCody(this);
        String conditionCode = SharedUtils.getConditionCode(this);
        String termId = SharedUtils.getTermId(this);
        String merchantNo = SharedUtils.getMerchantNo(this);
        String corpId = SharedUtils.getCorpId(this);
        param.put("cardId", cardId);
        param.put("txnAmt", "100");
        param.put("localDate", time.substring(2, 8));
        param.put("localTime", time.substring(8, 14));
        param.put("typeCode", typeCode);
        param.put("conditionCode", conditionCode);
        param.put("consumeType", "L");
        param.put("termId", termId);
        param.put("merchantNo", merchantNo);//商户号
        param.put("psamSeq", SharedUtils.getCardSeq(this) + "");


        param.put("corpId", corpId);
        param.put("deviceId", CommonUtil.getWifiMac(MainActivity.this));
        param.put("batchNo", time.substring(0, 8));
        param.put("signData", CommonUtil.md5(cardId + termId + corpId));
        OkHttpHelper.getInstance().post(this, Ip.CARD_CONSUMPTION, param, new NetCallBack() {
            @Override
            public void successCallBack(String successMsg) {
                Log.e("卡消费", successMsg);
                //TODO 保存消费 Long id, String cardId, String txnAmt, String time, boolean isUpload, boolean isQr
                BaseApplication.getDaoInstant().insert(new OrderInfo(null, cardId, (String) param.get("txnAmt"), time, true, false));
            }

            @Override
            public void failedCallBack(String failedMsg, int code) {
                Log.e("卡消费", failedMsg + code);
                //消费失败保存本地
                BaseApplication.getDaoInstant().insert(new OrderInfo(null, cardId, (String) param.get("txnAmt"), time, false, false));
            }
        });
        SharedUtils.setCardSeq(this, SharedUtils.getCardSeq(this) + 1);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mScanAnimator != null) {
            startScanUnKnowPermission();
        }
    }

    @Override
    public void onPause() {
        // Must be called here, otherwise the camera should not be released properly.
        stopScan();
        super.onPause();
    }

    /**
     * Stop scan.
     */
    private void stopScan() {
        if (mScanAnimator != null) {
            mScanAnimator.cancel();
            capturePreview.stop();
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mScanAnimator == null) {
            int height = capturePreview.getMeasuredHeight() - 25;
            mScanAnimator = ObjectAnimator.ofFloat(captureScanLine, "translationY", 0F, height).setDuration(3000);
            mScanAnimator.setInterpolator(new LinearInterpolator());
            mScanAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mScanAnimator.setRepeatMode(ValueAnimator.REVERSE);
            startScanUnKnowPermission();
        }
    }

    @OnClick(R.id.tv_main_msg)
    public void onViewClicked() {
        startActivity(new Intent(this, OrderListActivity.class));
    }

    @Override
    public void onSend(String send) {

    }

    @Override
    public void onReceive(int what, String rcv) {
        if (what == SendDataListener.TYPE_CARD_AUTO_REPORT) {
            if (rcv.length() == 6) { //按键
                String key = rcv.substring(0, 2);
                String value = KeyMap.getInstance().getValue(key);
            } else { //卡信息-atr
                int length = rcv.length();
                String atr = rcv.substring(0, length - 4);
//                cardConsume(atr);
            }
        } else if (what == SendDataListener.TYPE_CARD_RESPONSE) {
            if (rcv.equals("6F00")) {
                return;
            }
            if (rcv.matches("\\d+") && rcv.endsWith("9000")) {
                String cardNo = rcv.substring(0, rcv.length() - 4);
                return;
            } else {
                mCardReader.getCardNo();
            }
        }
    }

    /**
     * 获取网络时间
     */
    protected class GetTime extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPostExecute(String t) {
            if (!TextUtils.isEmpty(t))
                tvTime.setText(t);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://www.baidu.com");
                URLConnection uc = url.openConnection();//生成连接对象
                uc.setConnectTimeout(2000);
                uc.connect(); //发出连接
                long ld = uc.getDate(); //取得网站日期时间
                Date date = new Date(ld); //转换为标准时间对象
                return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    /**
     * 心跳
     */
    private void heart() {
        HashMap<String, String> param = new HashMap<String, String>();
        String deviceId = "M605L" + CommonUtil.getWifiMac(MainActivity.this) + "123456789012";
        String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        param.put("deviceId", deviceId);
        param.put("supplierNo", "00000000");//供应商编码
        param.put("dateTime", dateTime);
        param.put("merchantNo", SharedUtils.getMerchantNo(this));//商户号
        param.put("termNo", SharedUtils.getTermId(this));
        param.put("hardworkVer", CommonUtil.getAppName(this));//app
        param.put("whiteVer", SharedUtils.getWhiteVer(this));
        param.put("regionCode", "3170");
        String dataSign = getDataSign(deviceId + "00000000" + dateTime +
                SharedUtils.getMerchantNo(this) + SharedUtils.getTermId(this));
        param.put("dataSign", dataSign);
        OkHttpHelper.getInstance().post(this, Ip.SAVEHEARTBEAT_URL, param, new NetCallBack() {
            @Override
            public void successCallBack(String successMsg) {
                Log.e("心跳", successMsg);
                handler.sendEmptyMessageDelayed(1, 3 * 60 * 1000);
                Gson gson = new Gson();
                CommonBackJson<SaveHeart> backInfoObject = gson.fromJson(successMsg, new TypeToken<CommonBackJson<SaveHeart>>() {
                }.getType());
                if (backInfoObject.getCode() != null && backInfoObject.getCode().equals("00000")) {
                    download(backInfoObject.getContent());
                }
            }

            @Override
            public void failedCallBack(String failedMsg, int code) {
                Log.e("心跳", failedMsg + code);
                handler.sendEmptyMessageDelayed(1, 3 * 60 * 1000);

            }
        });

    }

    private void download(final SaveHeart saveHeart) {
        String name = saveHeart.getHardworkVer();
        if (!TextUtils.isEmpty(name)) {
            String ver = name.split("\\.")[0].substring(name.split("\\.")[0].length() - 3);
            if (!ver.equals(CommonUtil.getAppName(this).replace(".", ""))) {
                String merchantNo = SharedUtils.getMerchantNo(this);
                String deviceId = CommonUtil.getWifiMac(MainActivity.this);

                HashMap<String, String> paramsMap = new HashMap<String, String>();
                paramsMap.put("deviceId", deviceId);
                paramsMap.put("supplierNo", "00000000");//供应商编码
                paramsMap.put("merchantNo", merchantNo);//商户号
                paramsMap.put("fileName", saveHeart.getHardworkVer());
                String dataSign = getDataSign(deviceId + "00000000" + merchantNo + saveHeart.getHardworkVer());
                paramsMap.put("dataSign", dataSign);
                OkHttpHelper.getInstance().downloadFile(this, Ip.DOWNFILE_URL, paramsMap, new NetCallBack() {
                    @Override
                    public void successCallBack(String successMsg) {
                        Log.e("下载", successMsg);
                        CommonUtil.installSilently(Constant.PATH_DIR + successMsg);
                    }

                    @Override
                    public void failedCallBack(String failedMsg, int code) {
                        Log.e("下载", failedMsg + code);
                    }
                });
            }

        }

    }

    private static String getDataSign(String sourceData) {
        String macKay = "82040620FEFAC4511FC65000ADAB0F77";
        String dataSign = EncryptUtils.calculateMac(sourceData, macKay);
        return dataSign;
    }
}
