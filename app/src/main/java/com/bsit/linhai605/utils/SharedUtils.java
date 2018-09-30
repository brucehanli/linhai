package com.bsit.linhai605.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by DELL on 2017/9/7.
 */

public class SharedUtils {

    public static String getToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getString("token", "");
    }

    public static void setToken(Context context, String token) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.commit();
    }

    public static int getQrSeq(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getInt("qrSeq", 0);
    }

    public static void setQrSeq(Context context, int seq) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putInt("qrSeq", seq);
        editor.commit();
    }

    public static int getCardSeq(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getInt("cardSeq", 1);
    }

    public static void setCardSeq(Context context, int seq) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putInt("cardSeq", seq);
        editor.commit();
    }

    public static String getWhiteVer(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getString("whiteVer", "00000000000000000000");
    }

    public static void setWhiteVer(Context context, String whiteVer) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("whiteVer", whiteVer);
        editor.commit();
    }

    /////参数配置
    public static String getTypeCody(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getString("typeCode", "032");
    }

    public static void setTypeCody(Context context, String typeCode) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("typeCode", typeCode);
        editor.commit();
    }

    public static String getConditionCode(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getString("conditionCode", "00");
    }

    public static void setConditionCode(Context context, String conditionCode) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("conditionCode", conditionCode);
        editor.commit();
    }

    public static String getMerchantNo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getString("merchantNo", "774411885522");
    }

    public static void setMerchantNo(Context context, String merchantNo) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("merchantNo", merchantNo);
        editor.commit();
    }

    public static String getTermId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getString("termId", "10006001");
    }

    public static void setTermId(Context context, String termId) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("termId", termId);
        editor.commit();
    }

    public static String getCorpId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("linhai", Context.MODE_PRIVATE);
        return sp.getString("corpId", "330402010000006");
    }

    public static void setCorpId(Context context, String corpId) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("corpId", corpId);
        editor.commit();
    }

}
