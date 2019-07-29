package com.example.myapplication.Activity.Setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import com.example.myapplication.Activity.LogSign.LogActivity;
import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.R;

public class SettingActivity extends AppCompatActivity {

    private Boolean hasLogged;
    private SharedPreferences sharedPreferences;
    private boolean isInNight =  false;//是否处于夜间模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        hasLogged = sharedPreferences.getBoolean("HasLogged",false);
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_setting_night);
        }else {
            setContentView(R.layout.activity_setting);
        }



        Button logbutton = findViewById(R.id.LogButton);

        if(hasLogged){
            logbutton.setText(R.string.Logout);
        }
        else {
            logbutton.setText(R.string.Login);
        }

    }

    public void doPush(View view){
        if(hasLogged){//退出登录
            SharedPreferences.Editor editor =sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        else {//调转至登录界面
            Intent intent = new Intent(this, LogActivity.class);
            startActivity(intent);
        }
    }

    public void onBackPressed(View view){
        super.onBackPressed();
    }

    //切换到日间模式
    public void toDay(View view){
        LinearLayout background = findViewById(R.id.background);
        background.setBackgroundColor(Color.WHITE);

        Button logBtn = findViewById(R.id.LogButton);
        Button changeSkinBtn = findViewById(R.id.changeSkin);

        logBtn.setBackground(getDrawable(R.drawable.normal_btn_style));
        logBtn.setTextColor(Color.WHITE);
        changeSkinBtn.setBackground(getDrawable(R.drawable.normal_btn_style));
        changeSkinBtn.setText(getResources().getString(R.string.changeToNight));
        changeSkinBtn.setTextColor(Color.WHITE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("night",false);
        editor.apply();
    }

    //切换到夜间模式
    public void toNight(View view){
        LinearLayout background = findViewById(R.id.background);
        background.setBackgroundColor(Color.parseColor("#3F3E3E"));

        Button logBtn = findViewById(R.id.LogButton);
        Button changeSkinBtn = findViewById(R.id.changeSkin);

        logBtn.setBackground(getDrawable(R.drawable.normal_button_style_night));
        logBtn.setTextColor(Color.parseColor("#C2BEBE"));
        changeSkinBtn.setBackground(getDrawable(R.drawable.normal_button_style_night));
        changeSkinBtn.setText(getResources().getString(R.string.changeToDay));
        changeSkinBtn.setTextColor(Color.parseColor("#C2BEBE"));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("night",true);
        editor.apply();
    }
}
