package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/12.
 */

public class alarmInfo {
    String address;
    String latitude;
    String longitude;
    String alarmId;
    String poi;

    public alarmInfo(String address, String latitude, String longitude, String alarmId, String poi) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.alarmId = alarmId;
        this.poi = poi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        alarmId = alarmId;
    }

    public String getPoi() {
        return poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }
}
