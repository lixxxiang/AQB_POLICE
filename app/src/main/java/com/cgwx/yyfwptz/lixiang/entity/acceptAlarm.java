package com.cgwx.yyfwptz.lixiang.entity;

/**
 * Created by yyfwptz on 2017/7/12.
 */

public class acceptAlarm {
    String meta;
    String civilianTel;

    public acceptAlarm(String meta, String civilianTel) {
        this.meta = meta;
        this.civilianTel = civilianTel;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getCivilianTel() {
        return civilianTel;
    }

    public void setCivilianTel(String civilianTel) {
        this.civilianTel = civilianTel;
    }
}
