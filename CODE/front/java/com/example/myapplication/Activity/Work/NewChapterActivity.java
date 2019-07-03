package com.example.myapplication.Activity.Work;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.GetServer;
import com.example.myapplication.HttpUtils;
import com.example.myapplication.MyToast;

import com.example.myapplication.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class NewChapterActivity extends AppCompatActivity {

    private LinearLayout normal;
    private LinearLayout loadView;
    private int bookid;
    private String chapterTitle;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chapter);

        normal = findViewById(R.id.normal);
        loadView = findViewById(R.id.Loading);
        normal.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        bookid = intent.getIntExtra("bookid",-1);

        refresh();
    }

    @Override
    public void onBackPressed(){
        //保存草稿
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("保存草稿");
        builder.setMessage("是否保存为草稿?");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try{
                    //保存草稿数据至后端
                    new Thread(storeDraft).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new Thread(deleteDraft).start();
            }
        });

        builder.show();
    }

    public void onBackPressed(View view){
        onBackPressed();
    }

    Runnable deleteDraft = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/deleteDraft?bookid=" + bookid;

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        NewChapterActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable storeDraft = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/storeDraft";

                EditText text = findViewById(R.id.content);
                JSONObject info = new JSONObject();
                info.put("bookid",bookid);
                info.put("draft",text.getText().toString());
                byte[] param = info.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "GET", "application/json");//向后端发送请求

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(NewChapterActivity.this,getResources().getString(R.string.draft));
                        NewChapterActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //获取上次的草稿
    Runnable getDraft = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getDraft?bookid=" + bookid;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                final String draft = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText text = findViewById(R.id.content);
                        text.setText(draft);

                        loadView.setVisibility(View.INVISIBLE);
                        normal.setVisibility(View.VISIBLE);
                    }
                });


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    public void refresh(){
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

        new Thread(getDraft).start();
    }

    //保存章节
    public void storeChapter(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View v = LayoutInflater.from(this).inflate(R.layout.dialog_layout,null);
        TextView titleView = v.findViewById(R.id.Title);
        titleView.setText("标题");

        EditText text = findViewById(R.id.content);
        content = text.getText().toString();

        builder.setView(v);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText title = v.findViewById(R.id.Edit);
                chapterTitle = title.getText().toString();
                StoreChapter storeChapter = new StoreChapter(chapterTitle,bookid,content);
                new Thread(storeChapter).start();
            }
        });

        builder.setNegativeButton("取消", null);

        builder.show();
    }

    private class StoreChapter extends Thread
    {
        private String title;
        private int bookid;
        private String content;

        public StoreChapter(String title,int bookid,String content){
            this.title = title;
            this.bookid = bookid;
            this.content = content;
        }

        @Override
        public void run() {
            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/storeChapter";

                JSONObject info = new JSONObject();
                info.put("title",title);
                info.put("bookid",bookid);
                info.put("content",content);

                byte[]param = info.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST", "application/json");

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(NewChapterActivity.this,"创建成功!");
                        NewChapterActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void refresh(View view){
        refresh();
    }
}
