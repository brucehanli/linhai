package com.bsit.linhai605.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;


public class ToastUtils {
    /**
     * Toast提示消息
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg){
        if(TextUtils.isEmpty(msg)){
            return;
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Toast提示消息
     * @param context
     * @param sid
     */
    public static void showToast(Context context, int sid){
        Toast.makeText(context, context.getString(sid), Toast.LENGTH_SHORT).show();
    }
}
