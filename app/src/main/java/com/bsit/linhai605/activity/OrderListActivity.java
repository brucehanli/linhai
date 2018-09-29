package com.bsit.linhai605.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.bsit.linhai605.R;
import com.bsit.linhai605.adapter.MyOrderAdapter;
import com.bsit.linhai605.adapter.ViewHolder;
import com.bsit.linhai605.constant.Ip;
import com.bsit.linhai605.model.CommonBackJson;
import com.bsit.linhai605.model.ConsumeInfo;
import com.bsit.linhai605.net.NetCallBack;
import com.bsit.linhai605.net.OkHttpHelper;
import com.bsit.linhai605.utils.CommonUtil;
import com.bsit.linhai605.utils.SharedUtils;
import com.bsit.linhai605.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderListActivity extends Activity {
    @BindView(R.id.rv_order)
    RecyclerView rvOrder;
    private MyOrderAdapter orderAdapter;
    private List<ConsumeInfo> consumeInfos = new ArrayList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        ButterKnife.bind(this);
        initView();
        getOrderList("31700000011333961526");
    }

    private void initView() {
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
        rvOrder.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        orderAdapter = new MyOrderAdapter(this, R.layout.item_order, consumeInfos);
        orderAdapter.setOnClickListen(new ViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(View item, int position) {
                ToastUtils.showToast(OrderListActivity.this, "退款");
                cancelOrder(consumeInfos.get(position), "31700000011333961526");
            }
        });
        rvOrder.setAdapter(orderAdapter);
    }

    /**
     * 获取此卡此设备 当天交易记录
     */
    public void getOrderList(String cardId) {
/**
 * {"code":"1","message":null,"content":[
 * {"id":"402816166619bee0016619d030590001","cardId":"31700000011333961526",
 * "txnCode":null,"txnAmt":"-100","termSeq":"100003","txnTime":null,"txnDate":null,
 * "localTime":"145557","localDate":"180927","validDate":null,"clearDate":null,
 * "typeCode":null,"conditionCode":null,"pinCode":null,"centerSeq":null,"respCode":null,
 * "termId":"10006001","corpId":"330402010000006","currencyCode":null,"password":null,
 * "safeData":null,"otherAmt":null,"otherStaue":null,"batchNo":"20180927",
 * "oriMsgLand":null,"oriBatchNo":null,"oriTermSeq":null,"oriLocalDate":null,
 * "mac":null,"oriTermId":null,"oriCorpId":null,"flag":"1",
 * "signData":"8cc481864d047939b294dacaff26df3a","importTime":"2018-09-27 14:56:06",
 * "orderNo":"2018092714560604372157187096","type":"0201","cardSign":"0","userId":null,"
 * qrBornTime":null,"qrMessage":null,"merchantNo":"774411885522","deviceId":"b5b6a10ed82a046b",
 * "consumeType":"L","serMoney":"0","worKeyVersion":null},
 */
        final Map param = new HashMap<>();
        param.put("deviceId", CommonUtil.getWifiMac(this));
        param.put("merchantNo", "774411885522");//商户号
        param.put("cardId", cardId);

        OkHttpHelper.getInstance().post(this, Ip.GET_ORDER_LIST, param, new NetCallBack() {
            @Override
            public void successCallBack(String successMsg) {
                Log.e("列表", successMsg);
                Gson gson = new Gson();
                CommonBackJson<List<ConsumeInfo>> commonBackJson = gson.fromJson(successMsg, new TypeToken<CommonBackJson<List<ConsumeInfo>>>() {
                }.getType());
                if("1".equals(commonBackJson.getCode())){
                    consumeInfos.addAll(commonBackJson.getContent());
                    orderAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void failedCallBack(String failedMsg, int code) {
                Log.e("列表", failedMsg + code);
            }
        });


    }

    public void cancelOrder(ConsumeInfo info, String cardId) {
        final Map param = new HashMap<>();
        param.put("cardId", cardId);
        param.put("pasmSeq", SharedUtils.getCardSeq(this) + "");
        param.put("deviceId", CommonUtil.getWifiMac(this));
        param.put("merchantNo", "774411885522");//商户号
        param.put("orderNo", info.getOrderNo());
        param.put("typeCode", "032");
        param.put("conditionCode", "00");

        OkHttpHelper.getInstance().post(this, Ip.CANCEL_ORDER, param, new NetCallBack() {
            @Override
            public void successCallBack(String successMsg) {
                Log.e("撤销", successMsg);
            }

            @Override
            public void failedCallBack(String failedMsg, int code) {
                Log.e("撤销", failedMsg + code);
            }
        });


    }
}
