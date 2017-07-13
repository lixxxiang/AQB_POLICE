package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/13.
 */

public class EventUtil {
    String name;
    String telephone;
    String policeId;

    public EventUtil(String name, String telephone, String policeId) {
        this.name = name;
        this.telephone = telephone;
        this.policeId = policeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPoliceId() {
        return policeId;
    }

    public void setPoliceId(String policeId) {
        this.policeId = policeId;
    }
}
