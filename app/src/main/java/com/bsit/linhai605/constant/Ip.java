package com.bsit.linhai605.constant;

public class Ip {
    public static final String IP = "http://192.168.1.141:9003/pre_service_linhai/";

    public static final String LOGIN = IP + "login/loginPre_service_linhai";
    public static final String GET_QR_INFO = IP + "api/getQrInfo";//二维码消费
    public static final String CARD_CONSUMPTION = IP + "api/consumptionInfo";//卡消费
    public static final String GET_ORDER_LIST = IP + "api/getOrderInfoRecord";//获取卡消费列表
    public static final String CANCEL_ORDER = IP + "api/returnedPurchase";//撤销订单



//    public static final String HEART_IP = "http://121.43.37.101:9003/pre_service";
    public static final String HEART_IP = "http://192.168.1.166:8080/pre_service";

    public static final String SAVEHEARTBEAT_URL = HEART_IP + "/api_device/saveHeartBeat";
    public static final String DOWNFILE_URL = HEART_IP + "/api_device/downFile";

}
