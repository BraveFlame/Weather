package com.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.coolweather.R;

/**
 * Created by g on 2016/10/13.
 */

public class UpdateTimeActivity extends Activity {
    private Button sure,add,delete;
    private Switch onoff;
    private EditText settime;
    private String t="0";
    private boolean isOn = false;
//保留上次设置
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String temp=t;
        outState.putString("temp",temp);
        outState.putBoolean("isOn",isOn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.updateset);
        //取出上次设置
        if(savedInstanceState!=null){
            String temp=savedInstanceState.getString("temp");
            boolean is=savedInstanceState.getBoolean("isOn");
            settime.setText(temp);
            onoff.setChecked(is);
        }
        settime = (EditText) findViewById(R.id.settime);
        onoff = (Switch) findViewById(R.id.onoff);
        //判断是否自动更新
        onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    t = settime.getText().toString();
                    isOn = true;
                }
            }
        });
        sure = (Button) findViewById(R.id.updatetime);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOn == true) {
                    if (t==null||t.equals("0")) {
                        Toast.makeText(UpdateTimeActivity.this, "不能为0或空！！重新输入：", Toast.LENGTH_SHORT).show();
                        settime.setText("");
                    }
                    else {
                        Intent intent = new Intent();
                        intent.putExtra("time", t);
                        intent.putExtra("isOn", isOn);
                        setResult(RESULT_OK, intent);

                        finish();
                    }
                } else
                    finish();
            }
        });
        add=(Button)findViewById(R.id.add_city);
        delete=(Button)findViewById(R.id.delete_city);
    }

}
