package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/19.
 */

public class reserved {
    String latitude;
    String longitude;
    String address;
    String poi;
    String alarmId;
    String civilianTelephone;

    public reserved(String latitude, String longitude, String address, String poi, String alarmId, String civilianTelephone) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.poi = poi;
        this.alarmId = alarmId;
        this.civilianTelephone = civilianTelephone;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPoi() {
        return poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getCivilianTelephone() {
        return civilianTelephone;
    }

    public void setCivilianTelephone(String civilianTelephone) {
        this.civilianTelephone = civilianTelephone;
    }
}
