package com.example.myapplication.Activity.Account;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.R;
import org.json.JSONObject;
import com.example.myapplication.MyComponent.MyToast;
import com.example.myapplication.PicUtils.DrawAsCircle;
import com.example.myapplication.InternetUtils.HttpUtils;
import com.example.myapplication.PicUtils.GetPicture;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.example.myapplication.PicUtils.CropPic;

public class EditInfoActivity extends AppCompatActivity {

    private String accountNow;
    private String password;
    private TextView Account,Password,Name,Gender,Email;
    private LinearLayout normal;
    private LinearLayout loadView;
    private String avatarName;
    private int id;
    private final int GET_PICTURE=1;
    private final int CROP_PICTURE=2;
    private CropPic cropPic;
    private Uri avatarUri;
    private boolean isInNight = false;//是否处于夜间模式


    public void onBackPressed(View view){
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        accountNow = sharedPreferences.getString("Account","");
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_edit_info_night);
        }else {
            setContentView(R.layout.activity_edit_info);
        }

        normal = findViewById(R.id.Normal);
        loadView = findViewById(R.id.Loading);
        normal.setVisibility(View.INVISIBLE);



        Account = findViewById(R.id.Account);
        Password = findViewById(R.id.Password);
        Name = findViewById(R.id.Name);
        Gender = findViewById(R.id.Gender);
        Email = findViewById(R.id.Email);
        Account.setText(accountNow);


        loadView.setVisibility(View.VISIBLE);//加载画面
        findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

        cropPic = new CropPic();

        new Thread(setAvatar).start();
    }

    //获取当前用户头像
    Runnable setAvatar = new Runnable() {
        @Override
        public void run() {
            try {
                GetPicture getPicture = new GetPicture();
                final ImageView Avatar = findViewById(R.id.Avatar);
                final Bitmap avatar = getPicture.getAvatar(accountNow);
                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        if(avatar!=null) {
                            Avatar.setImageBitmap(avatar);
                        }
                        new Thread(setOldInfo).start();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //获取当前用户资料
    Runnable setOldInfo  = new Runnable() {
        @Override
        public void run() {
            try {
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/info?account="+ URLEncoder.encode(accountNow,"UTF-8");

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null,"GET",
                        "application/json");

                if(outputStream==null) {//请求超时
                    EditInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            msg(getResources().getString(R.string.HttpTimeOut));
                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                }

                else {
                String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject oldInfo = new JSONObject(result);

                EditInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {//更新UI
                        try {
                            password = oldInfo.getString("password");
                            Password.setText(password.replaceAll(".","*"));
                            Name.setText(oldInfo.getString("name"));
                            if (oldInfo.getString("gender").equals("male")) {
                                Gender.setText("男");
                            } else {
                                Gender.setText("女");
                            }
                            Email.setText(oldInfo.getString("email"));
                            id = oldInfo.getInt("id");

                            normal.setVisibility(View.VISIBLE);
                            loadView.setVisibility(View.INVISIBLE);//加载完毕
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //存储修改后的用户资料
    public void store(View view){
        final Button store = findViewById(R.id.Store);
        store.setClickable(false);

        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                store.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        try {
            new Thread(saveNewInfo).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //存储修改后的头像
    Runnable saveAvatar = new Runnable() {
        @Override
        public void run() {
            try {
                ImageView avatar = findViewById(R.id.Avatar);
                Bitmap newAvatar = ((BitmapDrawable) (avatar.getDrawable())).getBitmap();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                newAvatar.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] avatarStream = byteArrayOutputStream.toByteArray();


                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS() + "/audiobook/saveAvatar";

                HttpUtils httpUtils = new HttpUtils(url);
                avatarName=new String(httpUtils.doHttp(avatarStream,"POST",
                        "application/json").toByteArray(),
                        StandardCharsets.UTF_8);

                new Thread(saveAvatarName).start();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //存储头像路径
    Runnable saveAvatarName = new Runnable() {
        @Override
        public void run() {
            GetServer getServer = new GetServer();
            String url =getServer.getIPADDRESS() +
                    "/audiobook/saveAvatarName?id=" + id + "&avatar=" + avatarName;

            try {
                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(null,"POST","application/json");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //存储修改后的信息
    Runnable saveNewInfo = new Runnable() {
        @Override
        public void run() {
            EditInfoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout storing = findViewById(R.id.storing);
                    storing.setVisibility(View.VISIBLE);
                }
            });


            GetServer getServer = new GetServer();
            String url = getServer.getIPADDRESS()+"/audiobook/updateuser";

            try {
                JSONObject params = new JSONObject();
                params.put("oldAccount", accountNow);
                params.put("account", Account.getText());
                params.put("password", password);
                params.put("name", Name.getText());
                if (Gender.getText().equals("男")) {
                    params.put("gender", "male");
                } else {
                    params.put("gender", "female");
                }
                params.put("email", Email.getText());


                byte[] param = params.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(param, "POST",
                        "application/json");

                if (outputStream == null) {//请求超时
                    EditInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditInfoActivity.this, getResources().getString(R.string.HttpTimeOut));
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                EditInfoActivity.this.runOnUiThread(new Runnable() {//弹出修改结果
                    @Override
                    public void run() {
                        LinearLayout storing = findViewById(R.id.storing);
                        storing.setVisibility(View.INVISIBLE);

                        switch (result){
                            case "success":
                                changeAccount();
                                break;
                            case "fail":
                                msg(getResources().getString(R.string.EditFail));
                                break;
                            case "accountDul":
                                msg(getResources().getString(R.string.accountDul));
                                break;
                            case "emailDul":
                                msg(getResources().getString(R.string.emailDul));
                                break;
                        }
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //弹出提示信息
    public void msg(String msg){
        new MyToast(this,msg);
    }

    //建立dialog
    private AlertDialog buildDialog(String title){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = LayoutInflater.from(this).inflate(R.layout.dialog_layout,null);
        TextView titleView = v.findViewById(R.id.Title);
        titleView.setText(title);

        builder.setView(v);

        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", null);
        return builder.show();
    }

    //为dialog设置positiveBtn
    private void setPositiveBtn(final AlertDialog dialog,final String pattern, final String error, final TextView des){
        final EditText editText = dialog.findViewById(R.id.Edit);
        if(editText==null) return;
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().matches(pattern)) {
                    msg(error);
                }
                else {
                    if(des== Password) {
                        des.setText(editText.getText().toString().replaceAll(".","*"));
                        password = editText.getText().toString();
                    }
                    else {
                        des.setText(editText.getText());
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    //修改昵称窗口
    public void editAccount(View view){
        final AlertDialog dialog = buildDialog(getResources().getString(R.string.EditedAccount));
        setPositiveBtn(dialog,
                getResources().getString(R.string.accountPattern),
                getResources().getString(R.string.accountPatternError),
                Account);
    }

    //修改密码窗口
    public void editPassword(View view){
        final AlertDialog dialog = buildDialog(getResources().getString(R.string.EditedPassword));
        setPositiveBtn(dialog,
                getResources().getString(R.string.passwordPattern),
                getResources().getString(R.string.passwordPatternError),
                Password);
    }

    //修改姓名窗口
    public void editName(View view){
        final AlertDialog dialog = buildDialog(getResources().getString(R.string.EditedName));
        setPositiveBtn(dialog,
                getResources().getString(R.string.namePattern),
                getResources().getString(R.string.nameError),
                Name);
    }

    //修改性别
    public void editGender(View view){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = LayoutInflater.from(this).inflate(R.layout.gender_edit_dialog,null);

        TextView title = v.findViewById(R.id.Title);
        title.setText(R.string.EditedGender);

        final TextView male = v.findViewById(R.id.Male);
        final TextView female = v.findViewById(R.id.Female);

        builder.setView(v);

        final AlertDialog dialog = builder.show();

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gender.setText(R.string.Male);
                dialog.dismiss();
            }
        });

        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gender.setText(R.string.Female);
                dialog.dismiss();
            }
        });
    }

    //修改邮箱
    public void editEmail(View view){
        final AlertDialog dialog = buildDialog(getResources().getString(R.string.EditedEmail));
        setPositiveBtn(dialog,
                getResources().getString(R.string.emailPattern),
                getResources().getString(R.string.emailPatternError),
                Email);
    }

    /*修改头像*/
    //打开本地图片文件夹
    public void getPictureDir(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,GET_PICTURE);
    }

    //startActivityForResult回调函数
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        try {
            if (resultCode == RESULT_CANCELED) {
                return;
            }
            switch (requestCode) {
                case GET_PICTURE:
                    if (data != null) {
                        pictureCrop(data.getData());
                    }
                    break;
                case CROP_PICTURE:
                    if(avatarUri!=null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(avatarUri));
                        setAvatar(bitmap);
                    }
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //裁剪图片
    private void pictureCrop(Uri uri){
        try {
            Intent intent = new Intent();
            avatarUri= cropPic.setCropParam(intent,uri);
            startActivityForResult(intent, CROP_PICTURE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //设置头像
    private void setAvatar(Bitmap picture){
            DrawAsCircle drawAsCircle = new DrawAsCircle();
            Bitmap output = drawAsCircle.draw(picture);
            ImageView Avatar = findViewById(R.id.Avatar);
            Avatar.setImageBitmap(output);
            new Thread(saveAvatar).start();
    }

    //修改成功后修改全局变量Account
    public void changeAccount(){
        accountNow = Account.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putString("Account",Account.getText().toString());
        editor.apply();
        msg(getResources().getString(R.string.EditSuccess));
    }

    //刷新界面
    public void refresh(View view){
        loadView.setClickable(false);

        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                loadView.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

       loadView.setVisibility(View.VISIBLE);//加载画面
       findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
       findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

       new Thread(setAvatar).start();
    }
}
