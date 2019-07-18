package com.example.myapplication.Activity.Setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.example.myapplication.Activity.LogSign.LogActivity;
import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.R;

public class SettingActivity extends AppCompatActivity {

    private Boolean hasLogged;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        Button logbutton = findViewById(R.id.LogButton);
        hasLogged = sharedPreferences.getBoolean("HasLogged",false);

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
}
