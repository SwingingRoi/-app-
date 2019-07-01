package com.example.myapplication.Activity.LogSign;

import android.content.Intent;
import android.graphics.Paint;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.myapplication.CheckInternet;
import com.example.myapplication.SendEmail;
import com.example.myapplication.MyToast;
import com.example.myapplication.R;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import com.example.myapplication.HttpUtils;

import com.example.myapplication.GetServer;

public class SignActivity extends AppCompatActivity{

    private Boolean hasAccount, hasPassword, hasCheckPassword, hasName, hasEmail, hasPhone, hasVerify;
    private EditText account, password, checkPassword, name, email, phone, VerifyCode;
    private final int CODE_LENGTH = 4;
    private TextView[] Codes = new TextView[CODE_LENGTH];
    private Button signButton, getVerify;
    private ImageView clearAccount, clearCheckPassword, clearName, clearEmail, clearPhone;
    private RadioButton Male;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        hasAccount = false;
        hasPassword = false;
        hasCheckPassword = false;
        hasName = false;
        hasEmail = false;
        hasPhone = false;

        account = findViewById(R.id.SignAccount);
        account.addTextChangedListener(infowatcher);

        password = findViewById(R.id.SignPassword);
        password.addTextChangedListener(infowatcher);

        checkPassword = findViewById(R.id.ConfirmPassword);
        checkPassword.addTextChangedListener(infowatcher);

        name = findViewById(R.id.SignName);
        name.addTextChangedListener(infowatcher);

        email = findViewById(R.id.SignEmail);
        email.addTextChangedListener(infowatcher);

        phone = findViewById(R.id.SignPhone);
        phone.addTextChangedListener(infowatcher);

        VerifyCode = findViewById(R.id.VerifyCode);
        VerifyCode.setEnabled(false);
        VerifyCode.addTextChangedListener(codeWatcher);
        VerifyCode.addTextChangedListener(infowatcher);
        Codes[0] = findViewById(R.id.VerifyCode1);
        Codes[1] = findViewById(R.id.VerifyCode2);
        Codes[2] = findViewById(R.id.VerifyCode3);
        Codes[3] = findViewById(R.id.VerifyCode4);
        for (int i = 0; i < CODE_LENGTH; i++) {
            Codes[i].getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
            Codes[i].getPaint().setAntiAlias(true);
            Codes[i].setText(" ");
        }

        signButton = findViewById(R.id.SignUpload);
        getVerify = findViewById(R.id.GetVerify);
        clearAccount = findViewById(R.id.clearaccount);
        clearCheckPassword = findViewById(R.id.clearcomfirmpassword);
        clearName = findViewById(R.id.clearname);
        clearEmail = findViewById(R.id.clearemail);
        clearPhone = findViewById(R.id.clearphone);
        Male = findViewById(R.id.Male);
        setButtonState();
    }

    private TextWatcher codeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String code = VerifyCode.getText().toString();
            for (int i = 0; i < CODE_LENGTH; i++) {
                if (i < code.length()) {
                    Codes[i].setText(String.valueOf(code.charAt(i)));
                } else {
                    Codes[i].setText(" ");
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };//验证码监测

    private void setButtonState() {
        if (hasAccount && hasPassword && hasCheckPassword && hasName && hasEmail && hasPhone && hasVerify) {
            signButton.setEnabled(true);
        } else {
            signButton.setEnabled(false);
        }

        if (hasAccount && hasPassword && hasCheckPassword && hasName && hasEmail && hasPhone) {
            if (getVerify.getText().equals(getResources().getString(R.string.getVerify))) {
                getVerify.setEnabled(true);
            }
        } else {
            getVerify.setEnabled(false);
        }

        setVisible(clearAccount, hasAccount);
        setVisible(clearCheckPassword, hasCheckPassword);
        setVisible(clearName, hasName);
        setVisible(clearEmail, hasEmail);
        setVisible(clearPhone, hasPhone);
    }//设置按钮状态

    private void setVisible(ImageView imageView, boolean flag) {
        if (flag) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }
    }//设置按钮是否可见

    private TextWatcher infowatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            hasAccount = !(account.getText().toString().length() == 0);
            hasPassword = !(password.getText().toString().length() == 0);
            hasCheckPassword = !(checkPassword.getText().toString().length() == 0);
            hasName = !(name.getText().toString().length() == 0);
            hasEmail = !(email.getText().toString().length() == 0);
            hasPhone = !(phone.getText().toString().length() == 0);
            hasVerify = VerifyCode.getText().toString().length() == 4;
            setButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };//注册信息监测

    public void clearAccount(View view){
        account.setText("");
    }

    public void clearConfirmPassword(View view){
        checkPassword.setText("");
    }

    public void clearName(View view){
        name.setText("");
    }

    public void clearEmail(View view){
        email.setText("");
    }

    public void clearPhone(View view){
        phone.setText("");
    }

    public void passwordShow(View view) {
        EditText password = findViewById(R.id.SignPassword);
        EditText checkPassword = findViewById(R.id.ConfirmPassword);
        CheckBox checkBox = findViewById(R.id.passwordshowsign);
        if (!checkBox.isChecked()) {
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            checkPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            checkPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }//密码是否可见

    //注册信息合法性检验
    public void signCheck(View view) {
        if (!account.getText().toString().matches(getResources().getString(R.string.accountPattern))) {
            msg(getResources().getString(R.string.accountPatternError));
            return;
        }
        if (!password.getText().toString().matches(getResources().getString(R.string.passwordPattern))) {
            msg(getResources().getString(R.string.passwordPatternError));
            return;
        }
        if (!(password.getText().toString().equals(checkPassword.getText().toString()))) {
            msg(getResources().getString(R.string.checkpasswordError));
            return;
        }
        if (!email.getText().toString().matches(getResources().getString(R.string.emailPattern))) {
            msg(getResources().getString(R.string.emailPatternError));
            return;
        }
        sign();//信息合法，注册成功
    }

    //注册信息验证存储
    public void sign(){
        CheckInternet checkInternet = new CheckInternet();
        if(checkInternet.getIP()==null){
            msg("网络连接不可用!");
            return;
        }

        final Button signBtn = findViewById(R.id.SignUpload);
        signBtn.setClickable(false);
        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                signBtn.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        new Thread(sign).start();
    }

    Runnable sign = new Runnable() {
        @Override
        public void run() {
            GetServer getServer = new GetServer();
            String url=getServer.getIPADDRESS()+"/audiobook/sign";

            try {
                JSONObject newUser = new JSONObject();
                newUser.put("account", account.getText().toString());
                newUser.put("password", password.getText().toString());
                newUser.put("name", name.getText().toString());
                newUser.put("email", email.getText().toString());
                newUser.put("phone", phone.getText().toString());
                if (Male.isChecked()) {
                    newUser.put("gender", "male");
                } else {
                    newUser.put("gender", "female");
                }
                byte[] param = newUser.toString().getBytes();

               HttpUtils httpUtils = new HttpUtils(url);
               final String result = new String(httpUtils.doHttp(param,"POST","application/json").toByteArray(),
                       StandardCharsets.UTF_8);

                account.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (result){
                            case "timeout":
                                msg(getResources().getString(R.string.HttpTimeOut));
                                break;
                            case "success":
                                new Thread(sendEmail).start();
                                jumpAfterEmailSent();
                                break;
                            case "accountDul":
                                msg(getResources().getString(R.string.accountDul));
                                break;
                            case "emailDul":
                                msg(getResources().getString(R.string.emailDul));
                                break;
                            case "phoneDul":
                                msg(getResources().getString(R.string.phoneDul));
                                break;
                            default:
                                break;
                        }
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    public void getVerifyCode(View view) {
        CheckInternet checkInternet = new CheckInternet();
        if(checkInternet.getIP()==null){
            msg("网络连接不可用!");
            return;
        }

        String msg = getResources().getString(R.string.VerifyMsg) + phone.getText().toString();
        msg(msg);//验证码发送提醒

        VerifyCode.setEnabled(true);

        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getVerify.setEnabled(false);
                getVerify.setText(millisUntilFinished / 1000 + getResources().getString(R.string.second));//还剩millisUntilFinished / 1000秒可重新获取
            }

            @Override
            public void onFinish() {
                getVerify.setEnabled(true);
                getVerify.setText(R.string.getVerify);
            }
        };
        countDownTimer.start();
    }//获取手机验证码

    Runnable sendEmail = new Runnable() {
        @Override
        public void run() {
            SendEmail send = new SendEmail();
            send.sendEmail(email.getText().toString(),account.getText().toString());
        }
    };

    //发送邮件后跳转
    private void jumpAfterEmailSent(){
        Intent intent = new Intent(this, MsgAfterEmailActivity.class);
        intent.putExtra("email",email.getText().toString());
        startActivity(intent);
    }

    //弹出提示信息
    public void msg(String msg){
        new MyToast(this,msg);
    }

    public void onBackPressed(View view){
        super.onBackPressed();
    }

}
