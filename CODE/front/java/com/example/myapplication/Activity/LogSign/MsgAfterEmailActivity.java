package com.example.myapplication.Activity.LogSign;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.R;
public class MsgAfterEmailActivity extends AppCompatActivity {

    //发送激活邮件后跳转至该界面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_after_email);
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        TextView Email = findViewById(R.id.Email);
        Email.setText(email);
    }

    @Override
    public void onBackPressed(){//该界面只能返回至主界面
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
