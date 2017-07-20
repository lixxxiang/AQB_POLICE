package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/19.
 */

public class initStatus {
    String meta;
    reserved reservedAlarmInfo;
    String state;

    public initStatus(String meta, reserved reservedAlarmInfo, String state) {
        this.meta = meta;
        this.reservedAlarmInfo = reservedAlarmInfo;
        this.state = state;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public reserved getReservedAlarmInfo() {
        return reservedAlarmInfo;
    }

    public void setReservedAlarmInfo(reserved reservedAlarmInfo) {
        this.reservedAlarmInfo = reservedAlarmInfo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
