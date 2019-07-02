package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import com.example.myapplication.Activity.Work.NewChapterActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.PicUtils.GetPicture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;

public class BookActivity extends AppCompatActivity {

    private int INTRO=0;
    private int CHAPTER=1;
    private int WHICH=CHAPTER;

    private LinearLayout bookinfo;
    private TextView Intro;
    private TextView Chapter;
    private ImageView fav;
    private LinearLayout chapterTable;
    private ScrollView introScroll;
    private ScrollView chapterScroll;
    private LinearLayout loadingView;
    private View pullDown;//请求文字提示

    private int ISFAV=0;
    private boolean isRequesting = false;//判断当前是否在请求新章节
    private int from=0;
    final private int PAGESIZE=10;
    private int bookid;
    private JSONArray chapters;

    private String title;
    private String author;
    private String intro;
    private String account;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Intro = findViewById(R.id.intro);
        Chapter = findViewById(R.id.chapter);
        fav = findViewById(R.id.favicon);
        bookinfo = findViewById(R.id.bookinfo);

        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        account = sharedPreferences.getString("Account","");

        chapterTable = findViewById(R.id.chaptertable);

        pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down, null);
        chapterTable.addView(pullDown);

        introScroll = findViewById(R.id.introScroll);
        chapterScroll = findViewById(R.id.chapterScroll);
        chapterScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if (!isRequesting && chapterScroll.getChildAt(0).getHeight() <= chapterScroll.getHeight() + chapterScroll.getScrollY()) {
                        isRequesting = true;
                        new Thread(reqChapter).start();//向后端请求更多章节
                    }
                }
                return false;
            }
        });

        //设置顶部状态栏样式
        Window window=getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0XB3371D4E);

        loadingView = findViewById(R.id.Loading);
        loadingView.setVisibility(View.VISIBLE);
        bookinfo.setVisibility(View.INVISIBLE);
        chapterScroll.setVisibility(View.INVISIBLE);
        introScroll.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        bookid = intent.getIntExtra("id",0);
        refresh();
    }

    public void onBackPressed(View view){
        super.onBackPressed();
    }

    public void refresh(){

        loadingView.setClickable(false);

        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                loadingView.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        loadingView.setVisibility(View.VISIBLE);//加载画面
        findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
        findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

        chapterTable.removeAllViews();
        chapters = new JSONArray();
        from = 0;

        new Thread(getBookInfo).start();
        new Thread(checkFav).start();
        new Thread(addRecord).start();
        new Thread(reqChapter).start();
    }

    //刷新界面
    public void refresh(View view){
        refresh();
    }

    public void switchToIntro(View view){
        if(WHICH==INTRO) return;

        WHICH=INTRO;
        Intro.setTextSize(17);
        Intro.setTextColor(Color.BLACK);

        Chapter.setTextSize(14);
        Chapter.setTextColor(0xFFAAAAAA);

        introScroll.setVisibility(View.VISIBLE);
        chapterScroll.setVisibility(View.INVISIBLE);
    }

    public void switchToChapter(View view){
        if(WHICH==CHAPTER) return;

        WHICH=CHAPTER;
        Chapter.setTextSize(17);
        Chapter.setTextColor(Color.BLACK);

        Intro.setTextSize(14);
        Intro.setTextColor(0xFFAAAAAA);

        introScroll.setVisibility(View.INVISIBLE);
        chapterScroll.setVisibility(View.VISIBLE);
    }

    public void isFav(View view){
        fav.setClickable(false);
        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                fav.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        if(ISFAV==0){
            ISFAV=1;
            new MyToast(BookActivity.this,getResources().getString(R.string.fav));
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.favoritestarorange);
            fav.setImageBitmap(bitmap);
            new Thread(addFav).start();
        }
        else
        {
            ISFAV=0;
            new MyToast(BookActivity.this,getResources().getString(R.string.cancelfav));
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.favoritestarblack);
            fav.setImageBitmap(bitmap);
            new Thread(cancelFav).start();
        }
    }

    Runnable addFav = new Runnable() {
        @Override
        public void run() {
            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/addFavorite?account=" + URLEncoder.encode(account,"UTF-8") + "&id=" + bookid;


                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null,"GET",
                        "application/json");

                if(outputStream==null) {//请求超时
                    bookinfo.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this,getResources().getString(R.string.HttpTimeOut));
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable cancelFav = new Runnable() {
        @Override
        public void run() {
            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/cancelFavorite?account=" + URLEncoder.encode(account,"UTF-8");

                JSONArray idArray = new JSONArray();
                JSONObject idObject = new JSONObject();
                idObject.put("id",bookid);
                idArray.put(idObject);
                byte[] param = idArray.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(param,"GET",
                        "application/json");

                if(outputStream==null) {//请求超时
                    bookinfo.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this,getResources().getString(R.string.HttpTimeOut));
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable checkFav = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/checkFav?account=" + URLEncoder.encode(account,"UTF-8") + "&id=" + bookid;


                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null,"GET",
                        "application/json");

                if(outputStream==null) {//请求超时
                    bookinfo.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this,getResources().getString(R.string.HttpTimeOut));
                        }
                    });
                }

                else {
                    final String result = new String(outputStream.toByteArray(),
                            StandardCharsets.UTF_8);

                    bookinfo.post(new Runnable() {
                        @Override
                        public void run() {
                            if(result.equals("yes")){
                                ISFAV = 1;
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.favoritestarorange);
                                fav.setImageBitmap(bitmap);
                            }
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable getBookInfo = new Runnable() {
        @Override
        public void run() {

            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getbookbyid?id="
                        + bookid;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    bookinfo.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject book = new JSONObject(result);

                bookinfo.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(book.getInt("id") == -1){//说明该作品已被删除
                                loadingView.setVisibility(View.INVISIBLE);
                                View deleted = LayoutInflater.from(BookActivity.this).inflate(R.layout.book_deleted, null);
                                BookActivity.this.setContentView(deleted);
                            }

                            else {
                                TextView Title = findViewById(R.id.title);
                                title = book.getString("name");
                                Title.setText(title);

                                TextView Author = findViewById(R.id.author);
                                author = book.getString("author");
                                Author.setText(author);

                                if(author.equals(account)){//如果本书的作者是当前账号，则提供新建章节的选项
                                    TextView newChapter = findViewById(R.id.newChapter);
                                    newChapter.setVisibility(View.VISIBLE);
                                }

                                TextView introduction = findViewById(R.id.introduction);
                                intro = book.getString("intro");
                                introduction.setText(intro);

                                new Thread(getSurface).start();
                                new Thread(getAvatar).start();
                            }
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
            GetPicture getPicture = new GetPicture();
            final Bitmap surface = getPicture.getSurface(bookid);

            bookinfo.post(new Runnable() {
                @Override
                public void run() {
                    if(surface != null){
                        ImageView Surface = findViewById(R.id.surface);
                        Surface.setImageBitmap(surface);
                    }

                    loadingView.setVisibility(View.INVISIBLE);
                    bookinfo.setVisibility(View.VISIBLE);
                    chapterScroll.setVisibility(View.VISIBLE);

                }
            });
        }
    };

    Runnable getAvatar = new Runnable() {
        @Override
        public void run() {
            GetPicture getPicture = new GetPicture();
            final Bitmap avatar = getPicture.getAvatar(author);

            bookinfo.post(new Runnable() {
                @Override
                public void run() {
                    if(avatar !=null) {
                        ImageView Avatar = findViewById(R.id.avatar);
                        Avatar.setImageBitmap(avatar);
                    }
                }
            });
        }
    };

    Runnable addRecord = new Runnable() {
        @Override
        public void run() {
                  try{

                      GetServer getServer = new GetServer();
                      String url = getServer.getIPADDRESS()+"/audiobook/addrecord";

                      JSONObject params = new JSONObject();
                      params.put("account",account);
                      params.put("id",bookid);
                      Date date = new Date();
                      DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
                      params.put("time",format.format(date));

                      byte[] param = params.toString().getBytes();

                      HttpUtils httpUtils = new HttpUtils(url);
                      httpUtils.doHttp(param, "POST", "application/json");
                  }catch (Exception e){
                      e.printStackTrace();
                  }
        }
    };

    Runnable reqChapter = new Runnable() {
        @Override
        public void run() {
            chapterTable.post(new Runnable() {
                @Override
                public void run() {
                    TextView text = pullDown.findViewById(R.id.requestText);
                    text.setText(getResources().getString(R.string.isReq));
                }
            });

            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getChapters?bookid="
                        + bookid + "&from=" + from + "&size=" + PAGESIZE;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    chapterTable.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this, getResources().getString(R.string.HttpTimeOut));
                            isRequesting = false;
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONArray newChapters = new JSONArray(result);


                for (int i = 0; i < newChapters.length(); i++) {
                    chapters.put(newChapters.getJSONObject(i));
                }//向chapters中添加新请求过来的chapter

                chapterTable.post(new Runnable() {
                    @Override
                    public void run() {
                        chapterTable.removeView(pullDown);

                        for(int i=0;i < newChapters.length();i++){
                            try{
                                View chapterRow = LayoutInflater.from(BookActivity.this).inflate(R.layout.chapter_row, null);
                                TextView titleView = chapterRow.findViewById(R.id.chaptername);
                                titleView.setText(newChapters.getJSONObject(i).getString("title"));
                                TextView chapterNumber = chapterRow.findViewById(R.id.chapternumber);
                                chapterNumber.setText(String.valueOf(from + i + 1));
                                chapterTable.addView(chapterRow);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        chapterTable.addView(pullDown);
                        if(newChapters.length() < PAGESIZE){
                            TextView textView = pullDown.findViewById(R.id.requestText);
                            textView.setText(getResources().getString(R.string.hasEnd));
                            isRequesting = false;
                        }//说明章节已请求完毕
                        else {
                            TextView textView = pullDown.findViewById(R.id.requestText);
                            textView.setText(getResources().getString(R.string.pullDown));
                            isRequesting = false;
                        }

                        from = from + newChapters.length();//更新请求index
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    public void toNewChapter(View view){
        Intent intent = new Intent(this,NewChapterActivity.class);
        intent.putExtra("bookid",bookid);
        startActivity(intent);
    }
}
