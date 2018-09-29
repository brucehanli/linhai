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
        return sp.getInt("qrSeq", 1);
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

    public static void setWhiteVer(Context context, String seq) {
        SharedPreferences.Editor editor = context.getSharedPreferences("linhai", Context.MODE_PRIVATE).edit();
        editor.putString("whiteVer", seq);
        editor.commit();
    }

}
