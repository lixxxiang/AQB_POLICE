package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/12.
 */

public class getAlarm {
    String meta;
    alarmInfo alarmInfo;

    public getAlarm(String meta, alarmInfo alarmInfo) {
        this.meta = meta;
        this.alarmInfo = alarmInfo;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public com.cgwx.yyfwptz.lixiang.entity.alarmInfo getAlarmInfo() {
        return alarmInfo;
    }

    public void setAlarmInfo(com.cgwx.yyfwptz.lixiang.entity.alarmInfo alarmInfo) {
        this.alarmInfo = alarmInfo;
    }
}
