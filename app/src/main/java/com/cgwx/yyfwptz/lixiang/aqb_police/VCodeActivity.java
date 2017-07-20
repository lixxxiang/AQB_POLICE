package com.cgwx.yyfwptz.lixiang.aqb_police;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.cgwx.yyfwptz.lixiang.entity.Constants;
import com.cgwx.yyfwptz.lixiang.entity.checkMessage;
import com.cgwx.yyfwptz.lixiang.entity.policeInfo;
import com.cgwx.yyfwptz.lixiang.entity.sendMessage;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VCodeActivity extends AppCompatActivity {

    Button back;
    TextView tel;
    Button login;
    public static final String POST_URL = Constants.prefix + "mobile/police/checkMessage";


    private OkHttpClient client;
    Gson gson;
    String pTel;
    String pId;
    String pName;
    Long userId;
    String vcode;
    EditText vcodeedit;
    TextView countback;
    policeInfo pi;
    int count = 30;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            count--;
            countback.setText("" + count + "秒后 重新发送验证码");
            countback.setTextColor(Color.parseColor("#b5b5b5"));
            handler.postDelayed(this, 1000);

            if (count == 0) {
                countback.setText("重新发送验证码");
                countback.setTextColor(Color.parseColor("#ff9801"));
                handler.removeCallbacks(runnable);
                countback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gson = new Gson();
                        client = new OkHttpClient.Builder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                                .build();
                        RequestBody requestBodyPost = new FormBody.Builder()
                                .add("telephone", pTel)
                                .build();
                        Request requestPost = new Request.Builder()
                                .url(LoginActivity.POST_URL)
                                .post(requestBodyPost)
                                .build();
                        client.newCall(requestPost).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Toast.makeText(VCodeActivity.this, "验证码获取失败", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                final String string = response.body().string();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("rerereturn:", string);
                                        sendMessage userInfo = gson.fromJson(string, sendMessage.class);
                                        if (userInfo.getMeta().equals("success")) {
                                            handler.postDelayed(runnable, 1000);
                                            count = 30;
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vcode);
        tel = (TextView) findViewById(R.id.tel);
        back = (Button) findViewById(R.id.back);
        login = (Button) findViewById(R.id.login);
        vcodeedit = (EditText) findViewById(R.id.vcodeedit);
        countback = (TextView) findViewById(R.id.countback);
        Intent intent = getIntent();

        if (intent != null) {
            pTel = intent.getStringExtra("pTel");
            Log.e("to", pTel);
            tel.setText(pTel);
        }

        handler.postDelayed(runnable, 1000);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VCodeActivity.this.finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vcode = vcodeedit.getText().toString();
                gson = new Gson();
                client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBodyPost = new FormBody.Builder()
                        .add("telephone", pTel)
                        .add("code", vcode)
                        .build();
                Request requestPost = new Request.Builder()
                        .url(POST_URL)
                        .post(requestBodyPost)
                        .build();
                client.newCall(requestPost).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("Vcode", "failure");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String string = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("vcodereturn:", string);
                                checkMessage cm = gson.fromJson(string, checkMessage.class);
                                if (cm.getMeta().equals("success")) {
                                    pi = cm.getPoliceInfo();
                                    pId = pi.getPoliceId();
                                    pName = pi.getName();

                                    SharedPreferences sp = getSharedPreferences("Puser", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit(); //SharedPreferences 本身不能读写数据，需要使用Editor
                                    editor.putString("pTel", pTel);
                                    editor.putString("pId", pId);
                                    editor.putString("pName", pName);
                                    Log.e("v", pTel + pId + pName);
                                    editor.commit(); //提交

                                    Log.e("vcodereturnddddd:", pi.getPoliceId() + pi.getName() + pi.getTelephone());
                                    Intent intent = new Intent(VCodeActivity.this, MainActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putStringArray("pinfos", new String[]{pi.getPoliceId(), pi.getName(), pi.getTelephone()});
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(VCodeActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }
        });

        vcodeedit.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        vcodeedit.addTextChangedListener(mTextWatcher);



    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
//          mTextView.setText(s);//将输入的内容实时显示
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            login.setBackgroundResource(R.drawable.orabtn);
            if (vcodeedit.getText().length() == 0) {
                login.setBackgroundResource(R.drawable.gray);

            }

        }
    };
}
