package com.bsit.linhai605.model;

import java.io.Serializable;

public class SaveHeart implements Serializable {
    private String binVer;
    private String blackVer;
    private String whiteVer;
    private String commBlackVer;
    private String commWhiteVer;
    private String logoVer;
    private String deviceCommand;
    private String hardworkVer;
    private String fareVersion;

    public String getBinVer() {
        return binVer;
    }

    public void setBinVer(String binVer) {
        this.binVer = binVer;
    }

    public String getBlackVer() {
        return blackVer;
    }

    public void setBlackVer(String blackVer) {
        this.blackVer = blackVer;
    }

    public String getWhiteVer() {
        return whiteVer;
    }

    public void setWhiteVer(String whiteVer) {
        this.whiteVer = whiteVer;
    }

    public String getCommBlackVer() {
        return commBlackVer;
    }

    public void setCommBlackVer(String commBlackVer) {
        this.commBlackVer = commBlackVer;
    }

    public String getCommWhiteVer() {
        return commWhiteVer;
    }

    public void setCommWhiteVer(String commWhiteVer) {
        this.commWhiteVer = commWhiteVer;
    }

    public String getLogoVer() {
        return logoVer;
    }

    public void setLogoVer(String logoVer) {
        this.logoVer = logoVer;
    }

    public String getDeviceCommand() {
        return deviceCommand;
    }

    public void setDeviceCommand(String deviceCommand) {
        this.deviceCommand = deviceCommand;
    }

    public String getHardworkVer() {
        return hardworkVer;
    }

    public void setHardworkVer(String hardworkVer) {
        this.hardworkVer = hardworkVer;
    }

    public String getFareVersion() {
        return fareVersion;
    }

    public void setFareVersion(String fareVersion) {
        this.fareVersion = fareVersion;
    }
}
