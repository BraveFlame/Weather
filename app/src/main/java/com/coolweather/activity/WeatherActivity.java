package com.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.R;
import com.coolweather.service.AutoUpdateService;
import com.coolweather.utility.HttpCallbackListener;
import com.coolweather.utility.HttpUtil;
import com.coolweather.utility.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/*
聚合数据天气预报key 56ccb10d4a6633f35ddc7cf8ba480d45
http://op.juhe.cn/onebox/weather/query?cityname=%E6%B8%A9%E5%B7%9E&key=您申请的KEY
 */
public class WeatherActivity extends Activity implements OnClickListener {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private LinearLayout weatherInfoLayout;
    /*
     * 用于显示城市名
     */
    private TextView cityNameText;
    /*
     * 用于显示发布时间
     */
    private TextView publishText;
    /*
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /*
     * 用于显示湿度1和温度2，风向，风力
     */
    private TextView temp1Text, temp2View, direct, windPower, advice;
    /*
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /*
     * 选择城市，更新天气信息
     */
    private Button switchCity, refreshWeather;
    private TextView setting;
    private double t;
    private boolean isOn = false;
    Message message = new Message();
    String tt;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    showWeather();
                    handler.sendEmptyMessageDelayed(msg.what,(long)(t*60*60*1000));
                default:break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        Log.d("Weather", "reStart!");
        // 初始化各个控件
        message.what=0;
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2View = (TextView) findViewById(R.id.temp2);
        direct = (TextView) findViewById(R.id.direction);
        windPower = (TextView) findViewById(R.id.wind_power);
        advice = (TextView) findViewById(R.id.advice);
        currentDateText = (TextView) findViewById(R.id.current_date);
        String countyName = getIntent().getStringExtra("county_name");
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        isOn = pref.getBoolean("remember_on", false);

       t = pref.getFloat("Auto_time", 100000);
        if (isOn) {
            Intent intent = new Intent(this, AutoUpdateService.class);
            intent.putExtra("updateTime", t);
            startService(intent);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    message.what=1;
                    handler.sendMessage(message);

                }
            }).start();
        }

        setting = (TextView) findViewById(R.id.set_update_time);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        setting.setOnClickListener(this);
        if (!TextUtils.isEmpty(countyName)) {
            // 有县级代号就去查天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeather(countyName);
        } else {
            // 没有县级代号就直接显示本地天气
            showWeather();
        }
    }

    /*
     * 查询县级代号所对应的天气代号
     */
    private void queryWeather(String countyName) {
        try {
            String addresses = (URLEncoder.encode(countyName, "UTF-8"));
            String address = "http://op.juhe.cn/onebox/weather/query?cityname=" +
                    addresses + "&key=56ccb10d4a6633f35ddc7cf8ba480d45";
            queryFormServer(address, "add");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*
     * 根据传入的地址和类型去向服务器查询天气信息
     */
    private void queryFormServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if (type.equals("add")) {
                    // 处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this,
                            response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败！");
                    }
                });

            }
        });
    }

    /*
     * 从SharePreferences文件读取存储天气信息，并显示到界面
     */
    private void showWeather() {

        pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        cityNameText.setText(pref.getString("city_name", ""));
        temp1Text.setText(pref.getString("temp1", ""));
        temp2View.setText(pref.getString("temp2", ""));
        direct.setText(pref.getString("direction", ""));
        windPower.setText(pref.getString("wind_power", ""));
        advice.setText(pref.getString("advice", ""));
        weatherDespText.setText(pref.getString("weather_desp", ""));
        publishText.setText("今天" + pref.getString("publish_time", "") + "发布");
        currentDateText.setText(pref.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Toast.makeText(this, "更新成功!", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //切换城市
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            //更新数据
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(this);
                String countyName = pref.getString("city_name", "");
                if (!TextUtils.isEmpty(countyName)) {
                    queryWeather(countyName);
                }
                break;
            //设置自动更新与否（是则设置时间）
            case R.id.set_update_time:
                Intent updateIntent = new Intent(WeatherActivity.this, UpdateTimeActivity.class);
                //1用来判断数据来源
                startActivityForResult(updateIntent, 1);
            default:
                break;
        }

    }

    //接收更改设置自动更新的参数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //请求码是1则判断是否自动更新
            case 1:

                if (resultCode == RESULT_OK) {
                    editor = pref.edit();
                    //接收返回的text
                    tt = data.getStringExtra("autoUpdateTime");
                    try {
                        //转换成双精度浮点型
                        t = Double.valueOf(tt).doubleValue();
                        //保存自动更新的时间
                        editor.putFloat("Auto_time",(float) t);
                        editor.commit();
                        t=pref.getFloat("Auto_time",100000);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "格式错误！", Toast.LENGTH_SHORT).show();
                    }

                        //接收时间和isChecked状态
                        t=pref.getFloat("Auto_time",10000);
                        isOn = pref.getBoolean("remember_on", false);
                        //如果开启，保存当前设置
                        if (isOn == true) {
                            if(message.what==0) {
                                Toast.makeText(this, "设置成功！", Toast.LENGTH_SHORT).show();
                                //并开启自动更新线程，用于界面显示
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                            message.what = 1;
                                            handler.sendMessage(message);
                                    }
                                }).start();
                            }else {
                                Toast.makeText(this, "设置成功！", Toast.LENGTH_SHORT).show();
                            }
                            //是则开启服务自动更新，用于天气数据更新存储
                            Intent intent = new Intent(this, AutoUpdateService.class);
                            intent.putExtra("updateTime", t);
                            startService(intent);

                    }
                }else{

                        Intent intent = new Intent(this, AutoUpdateService.class);
                        Toast.makeText(this, "设置成功！", Toast.LENGTH_SHORT).show();
                        stopService(intent);
                         t=10000000;

                    }



                break;
            default:
                break;
        }


    }

}
