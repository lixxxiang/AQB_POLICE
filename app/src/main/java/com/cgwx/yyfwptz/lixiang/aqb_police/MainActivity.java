package com.cgwx.yyfwptz.lixiang.aqb_police;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.cgwx.yyfwptz.lixiang.entity.ActivityCollector;
import com.cgwx.yyfwptz.lixiang.entity.Constants;
import com.cgwx.yyfwptz.lixiang.entity.EventUtil;
import com.cgwx.yyfwptz.lixiang.entity.alarmInfo;
import com.cgwx.yyfwptz.lixiang.entity.initStatus;
import com.cgwx.yyfwptz.lixiang.entity.reserved;
import com.cgwx.yyfwptz.lixiang.entity.sendMessage;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MainFragment mainFragment;
    private MineFragment mineFragment;
    public static String infos[];
    String pname;
    String pid;
    String ptel;
    public static String status;
    public static MainActivity mainActivity;
    private static final String LTAG = MainActivity.class.getSimpleName();
    long exitTime = 0;
    public boolean isForeground = false;

    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            Log.d(LTAG, "action: " + s);
        }
    }

    private SDKReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        SharedPreferences sp = getSharedPreferences("Puser", MODE_PRIVATE);
        ptel = sp.getString("pTel", null);
        pid = sp.getString("pId", null);
        pname = sp.getString("pName", null);
        infos = new String[3];
        infos[0] = pid;
        infos[1] = pname;
        infos[2] = ptel;

//        Intent intent = getIntent();
//        Bundle bundle = intent.getExtras();
//        infos = bundle.getStringArray("pinfos");
        for (int i = 0; i < infos.length; i++) {
            Log.e("dddd", infos[i]);
        }

//        EventBus.getDefault().postSticky(new EventUtil("activity2发送消2息","activity2发送消息5","activity23发送消息"));

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
        initViews();
    }

    private void initViews() {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mainFragment = new MainFragment();
        mineFragment = new MineFragment();

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(mineFragment);
        fragments.add(mainFragment);

        ArrayList<String> titles = new ArrayList<>();
        titles.add("我的");
        titles.add("                              安全宝                              ");

        FragmentsAdapter mAdapter = new FragmentsAdapter(getSupportFragmentManager(), fragments, titles);
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager, false);
        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ff9801"));

        mViewPager.setCurrentItem(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        unregisterReceiver(mReceiver);
        Log.e("distroy", "a");

    }

    @Override
    protected void onStart() {
        super.onStart();
        isForeground = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            if ((System.currentTimeMillis() - exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeground = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
    }

    public boolean isForeground() {
        return isForeground;
    }
}
