package com.cgwx.yyfwptz.lixiang;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;

public class AQBApplication extends Application {
    public static RequestQueue queue;

    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        queue = Volley.newRequestQueue(getApplicationContext());
    }

    public static RequestQueue getHttpQueue() {
        return queue;
    }
}