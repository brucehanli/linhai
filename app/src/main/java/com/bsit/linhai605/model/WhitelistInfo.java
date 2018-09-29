package com.bsit.linhai605.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 白名单
 */
@Entity
public class WhitelistInfo {
    @Id(autoincrement = true)
    private Long id;
    private String cardId;
    @Generated(hash = 1312143847)
    public WhitelistInfo(Long id, String cardId) {
        this.id = id;
        this.cardId = cardId;
    }
    @Generated(hash = 106801376)
    public WhitelistInfo() {
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
}
