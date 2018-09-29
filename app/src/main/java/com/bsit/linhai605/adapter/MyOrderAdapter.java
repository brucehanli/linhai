package com.bsit.linhai605.adapter;

import android.content.Context;

import com.bsit.linhai605.R;
import com.bsit.linhai605.model.ConsumeInfo;

import java.util.List;

public class MyOrderAdapter extends CommonAdapter<ConsumeInfo> {
    ViewHolder.OnItemClickListener listener;

    public MyOrderAdapter(Context context, int layoutId, List<ConsumeInfo> datas) {
        super(context, layoutId, datas);
    }

    @Override
    public void convert(ViewHolder holder, int position, ConsumeInfo orderInfo) {
        if (orderInfo != null) {
            holder.setText(R.id.tv_order_money, "￥" +orderInfo.getTxnAmt());
            holder.setText(R.id.tv_order_time, orderInfo.getLocalDate()+orderInfo.getLocalTime());
            holder.setOnClickListener(position, R.id.ll_my_order, listener);
        }
    }

    public void setOnClickListen(ViewHolder.OnItemClickListener listener) {
        this.listener = listener;
    }


}
