package com.bsit.linhai605.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 订单详情 上传的保存一天 未上传的需达到上限
 */
@Entity
public class OrderInfo {
    @Id(autoincrement = true)
    private Long id;
    private String cardId;
    private String txnAmt;
    private String time;//YYYYMMDDHHmmss
    private boolean isUpload;
    private boolean isQr;
    @Generated(hash = 1053151114)
    public OrderInfo(Long id, String cardId, String txnAmt, String time,
            boolean isUpload, boolean isQr) {
        this.id = id;
        this.cardId = cardId;
        this.txnAmt = txnAmt;
        this.time = time;
        this.isUpload = isUpload;
        this.isQr = isQr;
    }
    @Generated(hash = 1695813404)
    public OrderInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCardId() {
        return this.cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
    public String getTxnAmt() {
        return this.txnAmt;
    }
    public void setTxnAmt(String txnAmt) {
        this.txnAmt = txnAmt;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public boolean getIsUpload() {
        return this.isUpload;
    }
    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }
    public boolean getIsQr() {
        return this.isQr;
    }
    public void setIsQr(boolean isQr) {
        this.isQr = isQr;
    }
    
   
}
