package com.cgwx.yyfwptz.lixiang.entity;

import android.support.v7.app.AppCompatActivity;

import com.cgwx.yyfwptz.lixiang.aqb_police.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yyfwptz on 2017/7/28.
 */

public class ActivityCollector {

    public static List<AppCompatActivity> activitys =
            new ArrayList<>();

    public static void addActivity(AppCompatActivity activity){
        activitys.add(activity);
    }

    public static void removeActivity(AppCompatActivity activity){
        if(activitys.contains(activity)){
            activitys.remove(activity);
        }
    }

    public static boolean activityInForeground(AppCompatActivity activity)     {
        return ((MainActivity)activity).isForeground;
    }

    public static boolean hasActivityInForeground(){
        for(AppCompatActivity activity : activitys ){
            if(activityInForeground(activity)){
                return true;
            }
        }
        return false;
    }
}