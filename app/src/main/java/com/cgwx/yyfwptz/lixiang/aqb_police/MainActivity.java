package com.cgwx.yyfwptz.lixiang.aqb_police;

import android.Manifest;
import android.content.Intent;
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
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.cgwx.yyfwptz.lixiang.entity.EventUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MainFragment mainFragment;
    private MineFragment mineFragment;
    public static String infos[] ;
    String pname;
    String pid;
    String ptel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        initViews();
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
        for (int i = 0; i < infos.length; i++){
            Log.e("dddd", infos[i]);
        }

//        EventBus.getDefault().postSticky(new EventUtil("activity2发送消2息","activity2发送消息5","activity23发送消息"));

    }
    private void initViews(){
        mTabLayout = (TabLayout)findViewById(R.id.tabs);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mainFragment = new MainFragment();
        mineFragment = new MineFragment();

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(mineFragment);
        fragments.add(mainFragment);

        ArrayList<String> titles = new ArrayList<>();
        titles.add("我的");
        titles.add("                              安全宝                              ");

        FragmentsAdapter mAdapter = new FragmentsAdapter(getSupportFragmentManager(),fragments,titles);
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager,false);
        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ff9801"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
