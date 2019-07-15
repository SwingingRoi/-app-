package com.example.myapplication.Activity.Work;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.GetServer;
import com.example.myapplication.HttpUtils;
import com.example.myapplication.PicUtils.BlurPic;
import com.example.myapplication.R;
import com.example.myapplication.PicUtils.CropPic;
import com.example.myapplication.MyToast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewBookActivity extends AppCompatActivity {

    private final int TITLE=1;
    private final int INTRO=2;
    private final int GET_SURFACE=3;
    private final int CROP_SURFACE=4;

    private CropPic cropPic;

    private String surfaceName="";

    private String account;

    private Uri imageUri=null;

    private TextView Title;
    private TextView Intro;
    private LinearLayout normal;

    private HashMap<String,Boolean> tagStats;//存储标签的选择状态
    private int count = 0;//已选择标签数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        Title = findViewById(R.id.Title);
        Intro = findViewById(R.id.Intro);
        normal = findViewById(R.id.normal);

        cropPic = new CropPic();

        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        account = sharedPreferences.getString("Account","");

        tagStats = new HashMap<>();
        initTagStat();
    }

    private void initTagStat(){
        tagStats.put(getResources().getString(R.string.booktag1),false);
        tagStats.put(getResources().getString(R.string.booktag2),false);
        tagStats.put(getResources().getString(R.string.booktag3),false);
        tagStats.put(getResources().getString(R.string.booktag4),false);
        tagStats.put(getResources().getString(R.string.booktag5),false);
        tagStats.put(getResources().getString(R.string.booktag6),false);
        tagStats.put(getResources().getString(R.string.booktag7),false);
        tagStats.put(getResources().getString(R.string.booktag8),false);//初始标签默认都没有选择
        count = 0;
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        builder.setMessage("确认退出吗?");
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                NewBookActivity.this.setResult(RESULT_CANCELED);
                new Thread(deleteSurface).start();
            }
        });
    }

    public void onBackPressed(View view){
        onBackPressed();
    }

    public void editTiltle(View view){
        Intent intent = new Intent(this,EditTitleActivity.class);
        intent.putExtra("title",Title.getText().toString());
        startActivityForResult(intent,TITLE);
    }

    public void editIntro(View view){
        Intent intent = new Intent(this,EditIntroActivity.class);
        intent.putExtra("intro",Intro.getText().toString());
        startActivityForResult(intent,INTRO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode==RESULT_CANCELED){
            return;
        }
        try {
            switch (requestCode) {
                case TITLE:
                    if (data.getExtras() != null) {
                        String title = data.getExtras().getString("title");
                        Title.setText(title);
                    }
                    break;

                case INTRO:
                    if (data.getExtras() != null) {
                        String intro = data.getExtras().getString("intro");
                        Intro.setText(intro);
                    }
                    break;

                case GET_SURFACE:
                    if (data != null) {
                        pictureCrop(data.getData());
                    }
                    break;

                case CROP_SURFACE:
                    if (data != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        setSurface(bitmap);
                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getPicture(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,GET_SURFACE);
    }

    //裁剪图片
    private void pictureCrop(Uri uri){
        try {
            Intent intent = new Intent();
            imageUri = cropPic.setCropParam(intent,uri);
            startActivityForResult(intent, CROP_SURFACE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //设置配图
    private void setSurface(Bitmap picture){
            if(picture!=null) {
                //小图
                ImageView surface = findViewById(R.id.booksurface);
                surface.setImageBitmap(picture);

                //模糊背景图
                /*来自
                https://github.com/PandaQAQ/BlurImage/blob/master/app/src/main/java/com/pandaq/blurimage/utils/FastBlur.java
                 */
                BlurPic blurPic = new BlurPic();
                int width = picture.getWidth();
                int height = picture.getHeight();
                float scale = 1;

                Bitmap overlay = Bitmap.createBitmap((int) (width / scale), (int) (height / scale), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(overlay);
                //将canvas按照bitmap等量缩放，模糊处理的图片才能显示正常
                canvas.scale(1 / scale, 1 / scale);
                Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
                canvas.drawBitmap(picture, 0, 0, paint);
                //对采样后的bitmap进行模糊处理，缩放采样后的图片处理比原图处理要省很多时间和内存开销
                overlay = blurPic.doBlur(overlay, 80, false);
                FrameLayout background = findViewById(R.id.surface);
                background.setBackground(new BitmapDrawable(getResources(),overlay));

                new Thread(storeSurface).start();
            }
        }

    //存储新书
    public void storeNewBook(View view){
        if(Title.getText().toString().length()==0){
            new MyToast(this,getResources().getString(R.string.titlenull));
            return;
        }

        if(Title.getText().toString().length() > 20){
            new MyToast(this,getResources().getString(R.string.namelong));
            return;
        }

        if(Intro.getText().toString().length() >200){
            new MyToast(this,getResources().getString(R.string.introlong));
            return;
        }

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
            chooseTags();//选择标签
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setTagClickListener(final TextView tagView){
        try{
            tagView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String tag = tagView.getText().toString();
                    boolean oldStat = tagStats.get(tag);

                    //设置标签样式，更新已选择标签数
                    if(oldStat == false){//选择标签
                        count++;
                        tagView.setTextColor(Color.RED);
                        tagStats.put(tag,true);//更新标签选择状态
                    }
                    else {//取消选择标签
                        count--;
                        tagView.setTextColor(Color.GRAY);
                        tagStats.put(tag,false);//更新标签选择状态
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void chooseTags(){
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View v = LayoutInflater.from(this).inflate(R.layout.book_tags,null);

            setTagClickListener((TextView) v.findViewById(R.id.tag1));
            setTagClickListener((TextView) v.findViewById(R.id.tag2));
            setTagClickListener((TextView) v.findViewById(R.id.tag3));
            setTagClickListener((TextView) v.findViewById(R.id.tag4));
            setTagClickListener((TextView) v.findViewById(R.id.tag5));
            setTagClickListener((TextView) v.findViewById(R.id.tag6));
            setTagClickListener((TextView) v.findViewById(R.id.tag7));
            setTagClickListener((TextView) v.findViewById(R.id.tag8));

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //new MyToast(NewBookActivity.this,tagStats.toString());
                    //检验是否超过三个标签被选择
                    if(count > 3) {
                        new MyToast(NewBookActivity.this, getResources().getString(R.string.remind));
                        initTagStat();
                        return;
                    }
                    new Thread(storeNewBook).start();
                }
            });


            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    initTagStat();
                }
            });//重置tagStats
            builder.setView(v);

            builder.show();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //先存储封面，并返回封面文件名
    Runnable storeSurface = new Runnable() {
        @Override
        public void run() {
            final String result;

            if(imageUri!=null) {
                ImageView surface = findViewById(R.id.booksurface);
                Bitmap newSurface = ((BitmapDrawable) (surface.getDrawable())).getBitmap();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                newSurface.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] avatarStream = byteArrayOutputStream.toByteArray();

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS() + "/audiobook/saveSurface";

                HttpUtils httpUtils = new HttpUtils(url);
                result = new String(httpUtils.doHttp(avatarStream, "POST",
                        "application/json").toByteArray(),
                        StandardCharsets.UTF_8);
            }//用户选择了封面

            else {
                result=null;
            }//用户未选择封面

            normal.post(new Runnable() {
                @Override
                public void run() {
                    if(result==null) surfaceName="";
                    else {
                        surfaceName = result;
                    }//返回封面文件名
                }
            });
        }
    };

    //再存储书本信息
    Runnable storeNewBook = new Runnable() {
        @Override
        public void run() {
            GetServer getServer = new GetServer();
            String url = getServer.getIPADDRESS()+"/audiobook/addbook";

            try{
                JSONObject params = new JSONObject();
                params.put("name",Title.getText().toString());
                params.put("intro",Intro.getText().toString());
                params.put("author",account);
                params.put("surface",surfaceName);

                String tags = "";
                if(tagStats.get(getResources().getString(R.string.booktag1)) == true){
                    tags += getResources().getString(R.string.booktag1) + " ";
                }
                if(tagStats.get(getResources().getString(R.string.booktag2)) == true){
                    tags += getResources().getString(R.string.booktag2) + " ";
                }
                if(tagStats.get(getResources().getString(R.string.booktag3)) == true){
                    tags += getResources().getString(R.string.booktag3) + " ";
                }
                if(tagStats.get(getResources().getString(R.string.booktag4)) == true){
                    tags += getResources().getString(R.string.booktag4) + " ";
                }
                if(tagStats.get(getResources().getString(R.string.booktag5)) == true){
                    tags += getResources().getString(R.string.booktag5) + " ";
                }
                if(tagStats.get(getResources().getString(R.string.booktag6)) == true){
                    tags += getResources().getString(R.string.booktag6) + " ";
                }
                if(tagStats.get(getResources().getString(R.string.booktag7)) == true){
                    tags += getResources().getString(R.string.booktag7) + " ";
                }
                if(tagStats.get(getResources().getString(R.string.booktag8)) == true){
                    tags += getResources().getString(R.string.booktag8) + " ";
                }
                params.put("tags",tags);

                byte[] param = params.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);

                ByteArrayOutputStream outputStream = httpUtils.doHttp(param,"POST",
                        "application/json");

                if(outputStream==null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewBookActivity.this,getResources().getString(R.string.HttpTimeOut));
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        Button store = findViewById(R.id.Store);
                        store.setClickable(true);


                        if(result.equals("success")) {
                            new MyToast(NewBookActivity.this, getResources().getString(R.string.makesuccess));
                            NewBookActivity.this.setResult(RESULT_OK);
                            NewBookActivity.this.finish();
                        }

                        if(result.equals("fail")){
                            new MyToast(NewBookActivity.this,getResources().getString(R.string.makefail));
                        }
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable deleteSurface = new Runnable() {
        @Override
        public void run() {
            if(surfaceName.length()!=0) {
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS() + "/audiobook/deletesurface?filename=" + surfaceName;

                try{
                    HttpUtils httpUtils = new HttpUtils(url);
                    httpUtils.doHttp(null, "GET",
                            "application/json");

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            normal.post(new Runnable() {
                @Override
                public void run() {
                    NewBookActivity.this.finish();
                }
            });
        }
    };
}
