package com.coolweather.service;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.coolweather.receiver.AutoUpdateReceiver;
import com.coolweather.utility.HttpCallbackListener;
import com.coolweather.utility.HttpUtil;
import com.coolweather.utility.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 1* 60 * 60 * 1000;// 延时更新后台
        //开启服务后广播onReceive执行
        long triggerAtTime =  SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }
    /*
     * 更新天气信息。
     */
    private void updateWeather() {
        try {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(this);
            String countyName = pref.getString("city_name", "");
            String addresses = (URLEncoder.encode(countyName, "UTF-8"));
            String address = "http://op.juhe.cn/onebox/weather/query?cityname=" + addresses + "&key=56ccb10d4a6633f35ddc7cf8ba480d45";
            HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {
                    Utility.handleWeatherResponse(AutoUpdateService.this, response);
                }
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
