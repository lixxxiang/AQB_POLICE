package com.cgwx.yyfwptz.lixiang.aqb_police;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.cgwx.yyfwptz.lixiang.entity.completeAlarm;
import com.cgwx.yyfwptz.lixiang.entity.modifyPosition;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProcessActivity extends AppCompatActivity {
    LocationClient mLocClient;
    private UiSettings mUiSettings;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    public static final String POST_URL_COMPLETEALARM = "http://10.10.90.11:8086/mobile/police/completeAlarm";

    ImageView call;
    MapView mMapView;
    BaiduMap mBaiduMap;
    boolean isFirstLoc = true;
    BitmapDescriptor start;
    BitmapDescriptor end;
    String infos[];
    Button navi;
    Button donePo;
    String string;

    private Marker mMarkerA;
    private Marker mMarkerB;
    private InfoWindow mInfoWindow;
    TextView poi;
    TextView address;
    TextView distance;
    TextView mOrkm;

    private OkHttpClient completeAlarmClient;
    Gson completeAlarmgson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_process);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        poi = (TextView) findViewById(R.id.poi);
        address = (TextView) findViewById(R.id.address);
        distance = (TextView) findViewById(R.id.distance);
        mOrkm = (TextView) findViewById(R.id.mOrkm);
        call = (ImageView) findViewById(R.id.call);

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ infos[9]));
                startActivity(intent);
            }
        });
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.move);
        start = BitmapDescriptorFactory.fromResource(R.drawable.start);
        end = BitmapDescriptorFactory.fromResource(R.drawable.end);
        navi = (Button) findViewById(R.id.navi);
        donePo = (Button) findViewById(R.id.donePo);
        donePo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeAlarmgson = new Gson();
                completeAlarmClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBodyPost2 = new FormBody.Builder()
                        .add("policeId", infos[8])
                        .add("alarmId", infos[7])
                        .build();
                Request requestPost2 = new Request.Builder()
                        .url(POST_URL_COMPLETEALARM)
                        .post(requestBodyPost2)
                        .build();
                completeAlarmClient.newCall(requestPost2).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String string = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("completeAlarmreturn:", string);
                                completeAlarm ca = completeAlarmgson.fromJson(string, completeAlarm.class);
                                if (ca.getMeta().equals("success")) {
                                    Log.e("state:", "报警成功");
                                    finish();
                                }
                            }
                        });
                    }

                });
            }
        });

        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng pt1 = new LatLng(Double.valueOf(infos[0]), Double.valueOf(infos[1]));
                LatLng pt2 = new LatLng(Double.valueOf(infos[2]), Double.valueOf(infos[3]));

                // 构建 导航参数
                NaviParaOption para = new NaviParaOption()
                        .startPoint(pt1).endPoint(pt2)
                        .startName("天安门").endName("百度大厦");

                try {
                    BaiduMapNavigation.openBaiduMapNavi(para, ProcessActivity.this);
                } catch (BaiduMapAppNotSupportNaviException e) {
                    e.printStackTrace();
                    showDialog();
                }

            }
        });
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker));
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setOverlookingGesturesEnabled(false);
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setAddrType("all");
        option.setIsNeedLocationPoiList(true);
        mLocClient.setLocOption(option);
        mLocClient.start();


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        infos = bundle.getStringArray("infos");
        for (int i = 0; i < infos.length; i++) {
            Log.e("dddd", infos[i]);
        }
//        Log.e("prooooo",intent.getStringExtra("startla")+intent.getStringExtra("startlo")+ intent.getStringExtra("endla")+intent.getStringExtra("endlo"));

        initOverlay(infos[0], infos[1], infos[2], infos[3]);

        if (Double.valueOf(infos[6]) > 1000) {
            distance.setText("" + Double.valueOf(infos[6]) / 1000);
            mOrkm.setText("千米");
        } else {
            distance.setText(infos[6]);
            mOrkm.setText("米");
        }
        poi.setText(infos[4]);
        if (infos[5].indexOf("市") != -1) {
            string = infos[5].substring(infos[5].indexOf("市") + 1, infos[5].length());
        }
        address.setText(" " + string);


    }


    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OpenClientUtil.getLatestBaiduMapApp(ProcessActivity.this);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }


    public void initOverlay(String sa, String so, String ea, String eo) {
        // add marker overlay
        LatLng startPo = new LatLng(Double.valueOf(sa), Double.valueOf(so));
        LatLng endPo = new LatLng(Double.valueOf(ea), Double.valueOf(eo));

        MarkerOptions startMarkerOptions = new MarkerOptions().position(startPo).icon(start)
                .zIndex(9).draggable(true);
        MarkerOptions endMarkerOptions = new MarkerOptions().position(endPo).icon(end)
                .zIndex(9).draggable(true);
        // 掉下动画
        startMarkerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
        endMarkerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);

        mMarkerA = (Marker) (mBaiduMap.addOverlay(startMarkerOptions));
        mMarkerB = (Marker) (mBaiduMap.addOverlay(endMarkerOptions));


        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(
                        ProcessActivity.this,
                        "拖拽结束，新位置：" + marker.getPosition().latitude + ", "
                                + marker.getPosition().longitude,
                        Toast.LENGTH_LONG).show();
            }

            public void onMarkerDragStart(Marker marker) {
            }
        });
    }

    public class MyLocationListenner implements BDLocationListener {
        public double lati;
        public double longi;
        public String address;
        List<Poi> poi;

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(0)
                    .direction(0).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            lati = location.getLatitude();
            longi = location.getLongitude();
            address = location.getAddrStr();
            poi = location.getPoiList();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
