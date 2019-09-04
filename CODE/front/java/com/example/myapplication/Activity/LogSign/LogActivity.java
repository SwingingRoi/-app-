package com.example.myapplication.Activity.LogSign;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.MyComponent.MyToast;
import com.example.myapplication.R;
import com.example.myapplication.EmailUtils.SendEmail;
import org.json.JSONObject;
import com.example.myapplication.InternetUtils.CheckInternet;
import com.example.myapplication.InternetUtils.HttpUtils;

import java.nio.charset.StandardCharsets;

public class LogActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Boolean hasAccount;
    private Boolean hasPassword;
    private EditText account;
    private EditText password;
    private CheckBox clear;
    private Button logButton,activeButton;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        hasAccount = false;
        hasPassword = false;

        account = findViewById(R.id.Logaccount);
        password = findViewById(R.id.Logpassword);
        clear = findViewById(R.id.Clear);
        logButton = findViewById(R.id.LogButton);
        activeButton = findViewById(R.id.ActiveButton);
        activeButton.setVisibility(View.INVISIBLE);
        setButtonState();

        account.addTextChangedListener(accountWatcher);
        password.addTextChangedListener(passwordWatcher);

        sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("HasLogged",false)) {
            editor = sharedPreferences.edit();
            editor.putBoolean("HasLogged", false);
            editor.apply();
        }
    }

    private void setButtonState(){
        if(hasAccount&&hasPassword){
            logButton.setEnabled(true);
        }
        else {
            logButton.setEnabled(false);
        }

        if(hasAccount){
            clear.setVisibility(View.VISIBLE);
            clear.setEnabled(true);
        }
        else {
            clear.setVisibility(View.INVISIBLE);
            clear.setEnabled(false);
        }
    }

    //监测用户名的输入
    private TextWatcher accountWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            hasAccount = !(account.getText().toString().length()==0);
        }

        @Override
        public void afterTextChanged(Editable s) {
            setButtonState();
        }
    };

    //监测密码的输入
    private TextWatcher passwordWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            hasPassword = !(password.getText().toString().length()==0);
        }

        @Override
        public void afterTextChanged(Editable s) {
            setButtonState();
        }
    };

    //密码是否可见
    public void passwordShow(View view){
        EditText password =findViewById(R.id.Logpassword);
        CheckBox check = findViewById(R.id.passwordshowforlog);
        if(!check.isChecked()){
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        else {
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    //登录信息验证
    public void logCheck(View view){
        CheckInternet checkInternet = new CheckInternet();
        if(checkInternet.getIP()==null){
            msg("网络连接不可用!");
            return;
        }

        final Button log = findViewById(R.id.LogButton);
        log.setClickable(false);

        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                log.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        new Thread(logCheck).start();
    }

    Runnable logCheck = new Runnable() {
        @Override
        public void run() {
            if(LogActivity.this.isFinishing()) return;
            LogActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout verifying = findViewById(R.id.verifying);
                    verifying.setVisibility(View.VISIBLE);
                }
            });

            GetServer getServer = new GetServer();
            String url=getServer.getIPADDRESS()+"/audiobook/log";

            try {
                JSONObject user = new JSONObject();
                user.put("account", account.getText().toString());
                user.put("password", password.getText().toString());
                byte[] param = user.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                final String result = new String(httpUtils.doHttp(param,"POST","application/json").toByteArray(),
                        StandardCharsets.UTF_8);

                if(LogActivity.this.isFinishing()) return;
                LogActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        LinearLayout verifying = findViewById(R.id.verifying);
                        verifying.setVisibility(View.INVISIBLE);

                        switch (result){
                            case "timeout":
                                msg(getResources().getString(R.string.HttpTimeOut));
                                break;
                            case "success":
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("HasLogged",true);
                                editor.putString("Account",account.getText().toString());
                                editor.apply();
                                jumpToMain();
                                break;
                            case "fail":
                                msg(getResources().getString(R.string.logError));
                                break;
                            case "none":
                                msg(getResources().getString(R.string.accountNotExist));
                                break;
                            default:
                                msg(getResources().getString(R.string.activeError));
                                activeButton.setVisibility(View.VISIBLE);
                                email = result;
                        }
                    }
                });


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //激活账号
    public void Active(View view){
        new Thread(sendEmail).start();
        Intent intent = new Intent(this, MsgAfterEmailActivity.class);
        intent.putExtra("email",email);
        startActivity(intent);
    }

    Runnable sendEmail = new Runnable() {
        @Override
        public void run() {
            if(LogActivity.this.isFinishing()) return;
            SendEmail send = new SendEmail();
            send.sendEmail(email,account.getText().toString());
        }
    };

    //跳转至首页
    public void jumpToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //跳转至注册界面
    public void jumpToSign(View view){
        Intent intent = new Intent(this,SignActivity.class);
        startActivity(intent);
    }

    //清空账号输入框
    public void clearAccount(View view){
        account.setText("");
    }

    //弹出提示信息
    public void msg(String msg){
        new MyToast(this,msg);
    }

    //返回上一个activity
    public void onBackPressed(View view){
        super.onBackPressed();
    }
}
