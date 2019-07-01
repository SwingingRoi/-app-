package com.example.myapplication.Activity.Work;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.GetServer;
import com.example.myapplication.HttpUtils;
import com.example.myapplication.MyToast;
import com.example.myapplication.PicUtils.BlurPic;
import com.example.myapplication.PicUtils.CropPic;
import com.example.myapplication.PicUtils.GetPicture;
import com.example.myapplication.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;


public class EditBookActivity extends AppCompatActivity {

    private LinearLayout loadView;
    private LinearLayout normal;
    private TextView Title;
    private TextView Intro;
    private ImageView Surface;
    private int bookid;

    private final int TITLE=1;
    private final int INTRO=2;
    private final int GET_SURFACE=3;
    private final int CROP_SURFACE=4;

    private CropPic cropPic;

    private boolean firstin = true;

    private String surfaceName="";
    private String initialSurface="";
    private Uri imageUri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        Intent intent = getIntent();
        bookid = intent.getIntExtra("id",0);

        loadView = findViewById(R.id.Loading);
        normal = findViewById(R.id.normal);
        Title = findViewById(R.id.Title);
        Intro = findViewById(R.id.Intro);
        Surface = findViewById(R.id.booksurface);

        loadView.setVisibility(View.VISIBLE);//加载画面
        findViewById(R.id.Remind).setVisibility(View.INVISIBLE);
        normal.setVisibility(View.INVISIBLE);
        Surface.setVisibility(View.INVISIBLE);

        cropPic = new CropPic();

        new Thread(getOldInfo).start();
        new Thread(getSurface).start();
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
                if(!surfaceName.equals(initialSurface)) {
                    new Thread(deleteSurface).start();
                }
                else {
                    EditBookActivity.this.finish();
                }
            }
        });

    }

    public void onBackPressed(View view){
        onBackPressed();
    }

    public void storeModify(View view){
        if(Title.getText().toString().length()==0){
            new MyToast(this,getResources().getString(R.string.titlenull));
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
            new Thread(storeNewBook).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Runnable getOldInfo = new Runnable() {
        @Override
        public void run() {
            GetServer getServer = new GetServer();
            String url = getServer.getIPADDRESS()+"/audiobook/getbookbyid?id=" + bookid;

            try{

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditBookActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject info = new JSONObject(result);

                normal.post(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Title.setText(info.getString("name"));
                            Intro.setText(info.getString("intro"));
                            surfaceName = info.getString("surface");
                            initialSurface = surfaceName;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable getSurface = new Runnable() {
        @Override
        public void run() {
            try {
                GetPicture getPicture = new GetPicture();
                final Bitmap surface = getPicture.getSurface(bookid);

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        setSurface(surface);
                        normal.setVisibility(View.VISIBLE);
                        Surface.setVisibility(View.VISIBLE);
                        loadView.setVisibility(View.INVISIBLE);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


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
            background.setBackground(new BitmapDrawable(getResources(), overlay));
        }

            if(firstin){
                firstin = false;
                return;
            }
            new Thread(storeSurface).start();
    }

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

    Runnable storeNewBook = new Runnable() {
        @Override
        public void run() {
            GetServer getServer = new GetServer();
            String url = getServer.getIPADDRESS()+"/audiobook/modifybook?id=" + bookid;

            try{
                JSONObject params = new JSONObject();
                params.put("name",Title.getText().toString());
                params.put("intro",Intro.getText().toString());
                params.put("surface",surfaceName);

                byte[] param = params.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);

                ByteArrayOutputStream outputStream = httpUtils.doHttp(param,"POST",
                        "application/json");

                if(outputStream==null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditBookActivity.this,getResources().getString(R.string.HttpTimeOut));
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        if(result.equals("success")) {
                            new MyToast(EditBookActivity.this, getResources().getString(R.string.EditSuccess));
                            EditBookActivity.this.finish();
                        }

                        if(result.equals("fail")){
                            new MyToast(EditBookActivity.this,getResources().getString(R.string.EditFail));
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
                    if(!initialSurface.equals(surfaceName)) {
                        HttpUtils httpUtils = new HttpUtils(url);
                        httpUtils.doHttp(null, "GET",
                                "application/json");
                    }

                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            EditBookActivity.this.finish();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };
}
