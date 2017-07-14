package com.cgwx.yyfwptz.lixiang.aqb_police;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
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
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.cgwx.yyfwptz.lixiang.entity.acceptAlarm;
import com.cgwx.yyfwptz.lixiang.entity.addAlarm;
import com.cgwx.yyfwptz.lixiang.entity.getAlarm;
import com.cgwx.yyfwptz.lixiang.entity.modifyPosition;
import com.cgwx.yyfwptz.lixiang.entity.refuseAlarm;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Button outpolice;
    View appear;
    View done;
    View view;
    Button listenPolice;
    Button quit;
    Button close;

    private OkHttpClient changeStateClient;
    private OkHttpClient modifyPositionClient;
    private OkHttpClient getAlarmClient;
    private OkHttpClient acceptAlarmClient;
    private OkHttpClient refuseAlarmClient;


    public static final String POST_URL_CHANGESTATE = "http://10.10.90.11:8086/mobile/police/modifyPoliceState/";
    public static final String POST_URL_MODIFYPOSITION = "http://10.10.90.11:8086/mobile/police/modifyPosition/";
    public static final String POST_URL_GETALARM = "http://10.10.90.11:8086/mobile/police/getAlarm";
    public static final String POST_URL_ACCEPTALARM = "http://10.10.90.11:8086/mobile/police/acceptAlarm";
    public static final String POST_URL_REFUSEALARM = "http://10.10.90.11:8086/mobile/police/refuseAlarm";

    String state;
    //    String fakePoliceId;
    String pid;
    Gson stategson;
    Gson getAlarmgson;
    Gson accpetAlarmgson;
    Gson refuseAlarmgson;

    LocationClient mLocClient;
    private UiSettings mUiSettings;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    MapView mMapView;
    BaiduMap mBaiduMap;
    Button button;
    boolean isFirstLoc = true; // 是否首次定位
    private OnFragmentInteractionListener mListener;
    SimpleDateFormat formatter;
    Date curDate;
    private Timer getAlarmtimer;
    private Timer getStatustimer;
    Timer countdownTimer;
    int count10sec;
    private TimerTask getAlarmtask;
    private TimerTask getStatustask;

    TextView alarmlocation;
    TextView alarmdistance;
    TextView dpoi;
    TextView ddis;
    TextView dadd;
    com.cgwx.yyfwptz.lixiang.entity.alarmInfo alarmInfo;

    Button acceptP;
    Button declineP;
    public double distance;

    int count = 0;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment, container, false);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        outpolice = (Button) getActivity().findViewById(R.id.outPolice);
        appear = getActivity().findViewById(R.id.appear);
        done = getActivity().findViewById(R.id.done);
        listenPolice = (Button) getActivity().findViewById(R.id.lisPolice);
        quit = (Button) getActivity().findViewById(R.id.quit);
        alarmdistance = (TextView) getActivity().findViewById(R.id.alarmDistance);
        alarmlocation = (TextView) getActivity().findViewById(R.id.alarmLocation);
        acceptP = (Button) getActivity().findViewById(R.id.accept);
        declineP = (Button) getActivity().findViewById(R.id.decline);
        dpoi = (TextView) getActivity().findViewById(R.id.dpoi);
        dadd = (TextView) getActivity().findViewById(R.id.dadr);
        ddis = (TextView) getActivity().findViewById(R.id.dd);
        close = (Button) getActivity().findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done.setVisibility(View.INVISIBLE);
            }
        });

        pid = MainActivity.infos[0];

        Log.e("lalala", "" + myListener.lati + myListener.longi);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mMapView = (MapView) getActivity().findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.location);
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker));
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        curDate = new Date(System.currentTimeMillis());
        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setOverlookingGesturesEnabled(false);
        mLocClient = new LocationClient(getActivity());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setAddrType("all");
        option.setIsNeedLocationPoiList(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        outpolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countdownTimer = new Timer();
                count = 0;
                getAlarmtimer = new Timer();
                getAlarmtask = new TimerTask() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Message message = new Message();
                        message.what = 1;
                        getAlarmhandler.sendMessage(message);
                    }
                };

                state = "2";
                Log.e("lalaladfafds", "" + myListener.lati + myListener.longi);
                String time = formatter.format(curDate);
                Log.e("time", time);
                stategson = new Gson();
                outpolice.setVisibility(View.INVISIBLE);
                listenPolice.setVisibility(View.VISIBLE);
                quit.setVisibility(View.VISIBLE);

                changeStateClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBodyPost = new FormBody.Builder()
                        .add("state", state)
                        .add("policeId", pid)

                        .build();
                Request requestPost = new Request.Builder()
                        .url(POST_URL_CHANGESTATE)
                        .post(requestBodyPost)
                        .build();
                changeStateClient.newCall(requestPost).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String string = response.body().string();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("changeStatereturn:", string);
                                addAlarm aA = stategson.fromJson(string, addAlarm.class);
                                if (aA.getMeta().equals("success")) {
                                    Log.e("state:", "报警成功");
                                }
                            }
                        });
                    }

                });


                modifyPositionClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
                Log.e("pid", pid);
                RequestBody requestBodyPost2 = new FormBody.Builder()
                        .add("latitude", "" + myListener.lati)
                        .add("longitude", "" + myListener.longi)
                        .add("policeId", pid)
                        .add("time", time)
                        .build();
                Request requestPost2 = new Request.Builder()
                        .url(POST_URL_MODIFYPOSITION)
                        .post(requestBodyPost2)
                        .build();
                modifyPositionClient.newCall(requestPost2).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String string = response.body().string();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("modifyPositionreturn:", string);
                                modifyPosition mp = stategson.fromJson(string, modifyPosition.class);
                                if (mp.getMeta().equals("success")) {
                                    Log.e("state:", "报警成功");
                                }
                            }
                        });
                    }

                });

                /**
                 * roll
                 */

                getAlarmtimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Message message = new Message();
                        message.what = 1;
                        getAlarmhandler.sendMessage(message);
                    }
                }, 1000, 1000);
            }
        });

        listenPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appear.VISIBLE == 0){
                    appear.setVisibility(View.INVISIBLE);
                }
                if (done.VISIBLE == 0){
                    done.setVisibility(View.INVISIBLE);

                }
                listenPolice.setText("听警中");
                state = "1";
                listenPolice.setVisibility(View.INVISIBLE);
                outpolice.setVisibility(View.VISIBLE);
                quit.setVisibility(View.INVISIBLE);
                changeStateClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBodyPost = new FormBody.Builder()
                        .add("state", state)
                        .add("policeId", pid)
                        .build();
                Request requestPost = new Request.Builder()
                        .url(POST_URL_CHANGESTATE)
                        .post(requestBodyPost)
                        .build();
                changeStateClient.newCall(requestPost).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String string = response.body().string();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("return:", string);

                            }
                        });
                    }

                });
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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

    Handler getAlarmhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 要做的事情
            super.handleMessage(msg);
            getAlarmRoll();
        }
    };


    Handler getStateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 要做的事情
            super.handleMessage(msg);
            Log.e("countdown", "" + count);
            getStateRoll();
            count++;
            if (count > 9) {
                getStatustimer.cancel();
                getStatustimer.purge();
                getStatustimer = null;
                Log.e("next", "next");
            }
        }
    };

    public void getStateRoll() {


    }

    public void getAlarmRoll() {
        countdownTimer = new Timer();
        getAlarmgson = new Gson();
        getAlarmClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        RequestBody requestBodyPost2 = new FormBody.Builder()
                .add("policeId", pid)
                .build();
        Request requestPost2 = new Request.Builder()
                .url(POST_URL_GETALARM)
                .post(requestBodyPost2)
                .build();
        getAlarmClient.newCall(requestPost2).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        getAlarm ga = getAlarmgson.fromJson(string, getAlarm.class);
                        Log.e("getAlarmreturn:", string);
                        if (ga.getMeta().equals("success")) {
                            Log.e("jiedaole", "sdfs");
                            getAlarmtimer.cancel();
                            count10sec = 11;
                            countdownTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    getActivity().runOnUiThread(new Runnable() {      // UI thread
                                        @Override
                                        public void run() {
                                            count10sec--;
                                            listenPolice.setText("" + count10sec);
                                            if (count10sec < 1) {
                                                countdownTimer.cancel();
                                                countdownTimer.purge();
//                                               countdownTimer = null;
                                                listenPolice.setText("听警中");
                                                refuseAlarmgson = new Gson();
                                                refuseAlarmClient = new OkHttpClient.Builder()
                                                        .connectTimeout(10, TimeUnit.SECONDS)
                                                        .readTimeout(10, TimeUnit.SECONDS)
                                                        .build();
                                                RequestBody requestBodyPost = new FormBody.Builder()
                                                        .add("alarmId", alarmInfo.getAlarmId())
                                                        .add("policeId", pid)
                                                        .build();
                                                Request requestPost = new Request.Builder()
                                                        .url(POST_URL_REFUSEALARM)
                                                        .post(requestBodyPost)
                                                        .build();
                                                refuseAlarmClient.newCall(requestPost).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        final String string = response.body().string();
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Log.e("return:", string);
                                                                refuseAlarm ra = refuseAlarmgson.fromJson(string, refuseAlarm.class);
                                                                if (ra.getMeta().equals("success")) {
                                                                    Log.e("state:", "jujiechenggong");
                                                                    appear.setVisibility(View.INVISIBLE);
                                                                    done.setVisibility(View.VISIBLE);
                                                                }
                                                            }
                                                        });
                                                    }

                                                });
//                        txtView.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }
                            }, 1000, 1000);
                            alarmInfo = ga.getAlarmInfo();
                            alarmlocation.setText("位置： " + alarmInfo.getPoi() + " " + alarmInfo.getAddress());
                            dadd.setText(alarmInfo.getAddress());
                            dpoi.setText("位置： "+alarmInfo.getPoi());


                            BNRoutePlanNode sNode = new BNRoutePlanNode(myListener.longi, myListener.lati, "", null, BNRoutePlanNode.CoordinateType.GCJ02);      //新建两个坐标点
                            BNRoutePlanNode eNode = new BNRoutePlanNode(Double.valueOf(alarmInfo.getLongitude()), Double.valueOf(alarmInfo.getLatitude()), "", null, BNRoutePlanNode.CoordinateType.GCJ02);
                            searchRoute(sNode, eNode);
//                            Log.e("distance", ""+);
                            acceptP.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    accpetAlarmgson = new Gson();
                                    acceptAlarmClient = new OkHttpClient.Builder()
                                            .connectTimeout(10, TimeUnit.SECONDS)
                                            .readTimeout(10, TimeUnit.SECONDS)
                                            .build();
                                    RequestBody requestBodyPost = new FormBody.Builder()
                                            .add("alarmId", alarmInfo.getAlarmId())
                                            .add("policeId", pid)
                                            .build();
                                    Request requestPost = new Request.Builder()
                                            .url(POST_URL_ACCEPTALARM)
                                            .post(requestBodyPost)
                                            .build();
                                    acceptAlarmClient.newCall(requestPost).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            final String string = response.body().string();
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.e("return:", string);
                                                    acceptAlarm aa = accpetAlarmgson.fromJson(string, acceptAlarm.class);

                                                    if (aa.getMeta().equals("success")) {
                                                        Log.e("state:", "报警成功");
                                                        getAlarmtimer.cancel();
                                                        Intent intent = new Intent(getActivity(), ProcessActivity.class);
                                                        Log.e("infos", "" + distance);
                                                        Bundle bundle = new Bundle();
                                                        bundle.putStringArray("infos", new String[]{
                                                                String.valueOf(myListener.lati),
                                                                String.valueOf(myListener.longi),
                                                                alarmInfo.getLatitude(),
                                                                alarmInfo.getLongitude(),
                                                                alarmInfo.getPoi(),
                                                                alarmInfo.getAddress(),
                                                                "" + distance,
                                                                alarmInfo.getAlarmId(),
                                                                pid,
                                                                aa.getCivilianTel()
                                                        });
                                                        intent.putExtras(bundle);
                                                        startActivity(intent);
//                                                        getAlarmtimer.cancel();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            });

                            declineP.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    listenPolice.setText("听警中");
                                    countdownTimer.cancel();
                                    countdownTimer.purge();
                                    refuseAlarmgson = new Gson();
                                    refuseAlarmClient = new OkHttpClient.Builder()
                                            .connectTimeout(10, TimeUnit.SECONDS)
                                            .readTimeout(10, TimeUnit.SECONDS)
                                            .build();
                                    RequestBody requestBodyPost = new FormBody.Builder()
                                            .add("alarmId", alarmInfo.getAlarmId())
                                            .add("policeId", pid)
                                            .build();
                                    Request requestPost = new Request.Builder()
                                            .url(POST_URL_REFUSEALARM)
                                            .post(requestBodyPost)
                                            .build();
                                    refuseAlarmClient.newCall(requestPost).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            final String string = response.body().string();
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.e("return:", string);
                                                    refuseAlarm ra = refuseAlarmgson.fromJson(string, refuseAlarm.class);
                                                    if (ra.getMeta().equals("success")) {
                                                        Log.e("state:", "jujiechenggong");
                                                        appear.setVisibility(View.INVISIBLE);
                                                        done.setVisibility(View.VISIBLE);

                                                    }
                                                }
                                            });
                                        }

                                    });

                                    getAlarmtimer = new Timer();
                                    getAlarmtimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            Message message = new Message();
                                            message.what = 1;
                                            getAlarmhandler.sendMessage(message);
                                        }
                                    }, 1000, 1000);



                                }
                            });
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(done.VISIBLE == 0)
                                done.setVisibility(View.INVISIBLE);
                            appear.setVisibility(View.VISIBLE);

//                            getStatustimer = new Timer();
//                            getStatustask = new TimerTask() {
//                                @Override
//                                public void run() {
//                                    // TODO Auto-generated method stub
//                                    Message message = new Message();
//                                    message.what = 1;
//                                    getStateHandler.sendMessage(message);
//                                }
//                            };
//                            getStatustimer.schedule(getStatustask, 1000, 1000);
                        }
                    }
                });
            }

        });
    }

    TimerTask countdownTask = new TimerTask() {
        @Override
        public void run() {


        }
    };

    private void searchRoute(BNRoutePlanNode sNode, BNRoutePlanNode eNode) {

        BNRoutePlanNode bp1 = sNode;
        BNRoutePlanNode bp2 = eNode;


        RoutePlanSearch search = RoutePlanSearch.newInstance();        //百度的搜索路线的类
        DrivingRoutePlanOption drivingRoutePlanOption = new DrivingRoutePlanOption();
        //起始坐标和终点坐标
        PlanNode startPlanNode = PlanNode.withLocation(new LatLng(bp1.getLatitude(), bp1.getLongitude()));  // lat  long
        PlanNode endPlanNode = PlanNode.withLocation(new LatLng(bp2.getLatitude(), bp2.getLongitude()));
        drivingRoutePlanOption.from(startPlanNode);
        drivingRoutePlanOption.to(endPlanNode);
        search.drivingSearch(drivingRoutePlanOption);


        search.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {   //搜索完成的回调
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {   //步行路线
                if (walkingRouteResult.getRouteLines() == null) return;
                int duration = walkingRouteResult.getRouteLines().get(0).getDuration();
                Toast.makeText(getActivity(), duration + "米", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {     //驾车路线
                if (drivingRouteResult.getRouteLines() == null) {
                    Toast.makeText(getActivity(), "算路失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                double duration = drivingRouteResult.getRouteLines().get(0).getDistance();
//                Toast.makeText(getActivity(), "距离是:" + duration + "米", Toast.LENGTH_SHORT).show();
                distance = duration;
                alarmdistance.setText("距离： " + "" + duration + "米（差一个计算！）");
                if (duration > 1000) {
                    ddis.setText("距离： " + duration / 1000 + "公里");
                } else {
                    ddis.setText("距离： " + duration + "米");
                }

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);//取消注册

    }

}
