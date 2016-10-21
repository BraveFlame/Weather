package com.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.coolweather.R;

/**
 * Created by g on 2016/10/13.
 */

public class UpdateTimeActivity extends Activity {
    private Button sure, add, delete;
    private Switch onoff;
    private EditText settime;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean isOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.updateset);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        settime = (EditText) findViewById(R.id.settime);
        onoff = (Switch) findViewById(R.id.onoff);
        //判断是否自动更新
        isOn = pref.getBoolean("remember_on", false);
        if (isOn) {
            String time = pref.getString("autoUpdateTime", "");
            settime.setText(time);
            onoff.setChecked(true);
        }
        sure = (Button) findViewById(R.id.updatetime);
        sure.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String time = settime.getText().toString();
                                        editor = pref.edit();
                                        if (onoff.isChecked()) {

                                            try {
                                                double a = Double.valueOf(time);
                                                if (a > 0) {
                                                    editor.putBoolean("remember_on", true);
                                                    editor.putString("autoUpdateTime", time);
                                                    editor.commit();
                                                    Intent intent = new Intent();
                                                    intent.putExtra("autoUpdateTime", time);
                                                    intent.putExtra("isOn", true);
                                                    setResult(RESULT_OK, intent);
                                                    finish();

                                                } else {
                                                    Toast.makeText(UpdateTimeActivity.this, "输入需大于0.5！", Toast.LENGTH_SHORT);

                                                }

                                            } catch (Exception e) {
                                                Toast.makeText(UpdateTimeActivity.this, "输入错误！", Toast.LENGTH_SHORT);
                                            }
                                        } else {
                                            editor.putBoolean("remember_on", false);
                                            editor.putString("autoUpdateTime", "100000");
                                            editor.commit();
                                            finish();
                                        }

                                    }
                                }

        );
        add = (Button) findViewById(R.id.add_city);

        delete = (Button) findViewById(R.id.delete_city);
    }

}
