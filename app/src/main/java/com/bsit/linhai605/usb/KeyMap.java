package com.bsit.linhai605.usb;

import java.util.HashMap;
import java.util.Map;

public class KeyMap {
    private static final KeyMap ourInstance = new KeyMap();

    public static KeyMap getInstance() {
        return ourInstance;
    }
    private Map<String,String> mKeyMap;
    private KeyMap() {
        mKeyMap = new HashMap<>();
        mKeyMap.put("01","2.5");
        mKeyMap.put("02","3.5");
        mKeyMap.put("03","4.5");
        mKeyMap.put("04","5.5");
        mKeyMap.put("05","6.5");
        mKeyMap.put("06","7.5");
        mKeyMap.put("07","返回");
        mKeyMap.put("08","主页");
        mKeyMap.put("09","0");
        mKeyMap.put("0A","1");
        mKeyMap.put("0B","2");
        mKeyMap.put("0C","3");
        mKeyMap.put("0D","4");
        mKeyMap.put("0E","5");
        mKeyMap.put("0F","6");
        mKeyMap.put("10","7");
        mKeyMap.put("11","8");
        mKeyMap.put("12","9");
        mKeyMap.put("13",".");
        mKeyMap.put("14","+");
        mKeyMap.put("15","-");
        mKeyMap.put("16","*");
        mKeyMap.put("17","/");
        mKeyMap.put("18","=");
        mKeyMap.put("19","静音");
        mKeyMap.put("1A","设置");
        mKeyMap.put("1B","查询");
        mKeyMap.put("1C","充值");
        mKeyMap.put("1D","结算");
        mKeyMap.put("1F","C");
    }

    public String getValue(String key){
        return mKeyMap.get(key);
    }
}
