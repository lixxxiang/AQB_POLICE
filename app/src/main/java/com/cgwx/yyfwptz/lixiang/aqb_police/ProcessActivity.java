package com.cgwx.yyfwptz.lixiang.aqb_police;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.baidu.mapapi.map.MapStatusUpdate;
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
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.cgwx.yyfwptz.lixiang.entity.MyOrientationListener;
import com.cgwx.yyfwptz.lixiang.entity.OverlayManager;
import com.cgwx.yyfwptz.lixiang.entity.RouteLineAdapter;
import com.cgwx.yyfwptz.lixiang.entity.WalkingRouteOverlay;
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

public class ProcessActivity extends AppCompatActivity implements OnGetRoutePlanResultListener{
    LocationClient mLocClient;
    private UiSettings mUiSettings;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    public static final String POST_URL_COMPLETEALARM = "http://10.10.90.11:8086/mobile/police/completeAlarm";

    ImageView call;

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


    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = true;
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    WalkingRouteResult nowResultwalk = null;

    private OkHttpClient completeAlarmClient;
    Gson completeAlarmgson;

    private MapView mMapView=null;
    private BaiduMap mBaiduMap;
    private LocationClient mlocationClient;
    private MylocationListener mlistener;
    private Context context;
    private double mLatitude;
    private double mLongitude;
    private float mCurrentX;
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private MyLocationConfiguration.LocationMode locationMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_process);
        this.context=this;
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
//        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
//        mMapView = (MapView) findViewById(R.id.bmapView);
//        mBaiduMap = mMapView.getMap();
//        mBaiduMap.setMyLocationEnabled(true);
//        mCurrentMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.move);
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
        initView();
        initLocation();
//        mBaiduMap
//                .setMyLocationConfigeration(new MyLocationConfiguration(
//                        mCurrentMode, true, mCurrentMarker));
//        View child = mMapView.getChildAt(1);
//        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
//            child.setVisibility(View.INVISIBLE);
//        }
//
//        mMapView.showScaleControl(false);
//        mMapView.showZoomControls(false);
//        mUiSettings = mBaiduMap.getUiSettings();
//        mUiSettings.setScrollGesturesEnabled(false);
//        mUiSettings.setOverlookingGesturesEnabled(false);
//        mLocClient = new LocationClient(this);
//        mLocClient.registerLocationListener(myListener);
//        LocationClientOption option = new LocationClientOption();
//        option.setOpenGps(true); // 打开gps
//        option.setCoorType("bd09ll"); // 设置坐标类型
//        option.setScanSpan(1000);
//        option.setAddrType("all");
//        option.setIsNeedLocationPoiList(true);
//        mLocClient.setLocOption(option);
//        mLocClient.start();


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

        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        PlanNode stNode = PlanNode.withLocation(new LatLng(Double.valueOf(infos[0]), Double.valueOf(infos[1])));
        PlanNode enNode = PlanNode.withLocation(new LatLng(Double.valueOf(infos[2]),Double.valueOf(infos[3])));
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode).to(enNode));
    }
    private void initView() {
        mMapView= (MapView) findViewById(R.id.bmapView);
        mBaiduMap=mMapView.getMap();
        MapStatusUpdate msu= MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        getMyLocation();


    }

    public void getMyLocation()
    {
        LatLng latLng=new LatLng(mLatitude,mLongitude);
        MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);
    }

    public class MylocationListener implements BDLocationListener
    {
        private boolean isFirstIn=true;
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLatitude= bdLocation.getLatitude();
            mLongitude=bdLocation.getLongitude();
            MyLocationData data= new MyLocationData.Builder()
                    .direction(mCurrentX)//设定图标方向
                    .accuracy(bdLocation.getRadius())//getRadius 获取定位精度,默认值0.0f
                    .latitude(mLatitude)//百度纬度坐标
                    .longitude(mLongitude)//百度经度坐标
                    .build();
            mBaiduMap.setMyLocationData(data);
            MyLocationConfiguration configuration
                    =new MyLocationConfiguration(locationMode,true,mIconLocation);
            mBaiduMap.setMyLocationConfigeration(configuration);
            if(isFirstIn)
            {
                LatLng latLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.setMapStatus(msu);
                isFirstIn=false;
                Toast.makeText(context, bdLocation.getAddrStr(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mBaiduMap.setMyLocationEnabled(true);
        if(!mlocationClient.isStarted())
        {
            mlocationClient.start();
        }
        myOrientationListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBaiduMap.setMyLocationEnabled(false);
        mlocationClient.stop();
        myOrientationListener.stop();
    }

    private void initLocation() {
        locationMode= MyLocationConfiguration.LocationMode.NORMAL;
        mlocationClient=new LocationClient(this);
        mlistener=new MylocationListener();
        mlocationClient.registerLocationListener(mlistener);
        LocationClientOption mOption=new LocationClientOption();
        mOption.setCoorType("bd09ll");
        mOption.setIsNeedAddress(true);
        mOption.setOpenGps(true);
        int span=1000;
        mOption.setScanSpan(span);
        mlocationClient.setLocOption(mOption);
        mIconLocation= BitmapDescriptorFactory
                .fromResource(R.drawable.move);
        myOrientationListener=new MyOrientationListener(context);
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX=x;
            }
        });
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
//        startMarkerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
//        endMarkerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);

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

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(ProcessActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;

            if (result.getRouteLines().size() > 1 ) {
                nowResultwalk = result;

                MyTransitDlg myTransitDlg = new MyTransitDlg(ProcessActivity.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.WALKING_ROUTE);
                myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                    public void onItemClick(int position) {
                        route = nowResultwalk.getRouteLines().get(position);
                        WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                        mBaiduMap.setOnMarkerClickListener(overlay);
                        routeOverlay = overlay;
                        overlay.setData(nowResultwalk.getRouteLines().get(position));
                        overlay.addToMap();
                        overlay.zoomToSpan();
                    }

                });
                myTransitDlg.show();

            } else if ( result.getRouteLines().size() == 1 ) {
                // 直接显示
                route = result.getRouteLines().get(0);
                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else {
                Log.d("route result", "结果数<0" );
                return;
            }

        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

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


    }




    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.start);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.end);
            }
            return null;
        }
    }


    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private  RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List< ? extends RouteLine> transitRouteLines,  RouteLineAdapter.Type
                type) {
            this( context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new  RouteLineAdapter( context, mtransitRouteLines , type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener( new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemInDlgClickListener.onItemClick( position);
                    dismiss();

                }
            });
        }

        public void setOnItemInDlgClickLinster( OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }

    }


}
