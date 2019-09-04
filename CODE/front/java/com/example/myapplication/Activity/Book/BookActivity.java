package com.example.myapplication.Activity.Book;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import com.example.myapplication.Activity.Work.NewChapterForTTSActivity;
import com.example.myapplication.Activity.Work.NewChapterForSTTActivity;
import com.example.myapplication.Activity.Work.EditBookActivity;

import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.Activity.LogSign.LogActivity;
import com.example.myapplication.Activity.Work.ChapterActivity;
import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.InternetUtils.HttpUtils;
import com.example.myapplication.MyComponent.MySpinner;
import com.example.myapplication.MyComponent.MyToast;
import com.example.myapplication.PicUtils.GetPicture;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity {

    private int INTRO=0;
    private int CHAPTER=1;
    private int WHICH=CHAPTER;

    private LinearLayout bookinfo;
    private TextView Intro;
    private TextView Chapter;
    private ImageView fav;
    private LinearLayout chapterTable;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView introScroll;
    private ScrollView chapterScroll;
    private LinearLayout loadingView;
    private MySpinner menu;
    private LinearLayout manageBox;
    private View pullDown;//请求文字提示

    private int ISFAV=0;
    private boolean isRequesting = false;//判断当前是否在请求新章节
    private int from=0;
    final private int PAGESIZE=10;
    private int bookid;
    private JSONArray chapters;
    private boolean isInNight;//是否处于夜间模式
    private float preY;//用户触摸屏幕时的手指纵坐标
    private float nowY;//用户手指离开屏幕时的纵坐标

    private String title;
    private String author;
    private String intro;
    private String account;
    private boolean hasClickMenu = false;//是否点击菜单
    private boolean ismanaging = false;//是否处于管理模式

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        account = sharedPreferences.getString("Account","");
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式


        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_book_night);
        }else {
            setContentView(R.layout.activity_book);
        }

        Intro = findViewById(R.id.intro);
        Chapter = findViewById(R.id.chapter);
        fav = findViewById(R.id.favicon);
        bookinfo = findViewById(R.id.bookinfo);

        swipeRefreshLayout = findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        manageBox = findViewById(R.id.manage);
        menu = findViewById(R.id.menu);
        menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!hasClickMenu){
                    hasClickMenu = true;
                    return;
                }//取消spinner的默认选择

                String select = parent.getItemAtPosition(position).toString();

                switch (select){
                    case "新建章节":
                        toNewChapter();
                        break;
                    case "管理章节":
                        if(!ismanaging){
                            toManage();
                        }
                        break;
                    case "编辑图书":
                        Intent intent = new Intent(BookActivity.this, EditBookActivity.class);
                        intent.putExtra("id", bookid);
                        startActivity(intent);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chapterTable = findViewById(R.id.chaptertable);

        if(isInNight){
            pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down_night, null);
        }else {
            pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down, null);
        }
        chapterTable.addView(pullDown);

        introScroll = findViewById(R.id.introScroll);
        chapterScroll = findViewById(R.id.chapterScroll);
        chapterScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        preY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        nowY = event.getY();
                        if(nowY < preY){
                            if (!isRequesting && chapterScroll.getChildAt(0).getMeasuredHeight() <= chapterScroll.getScrollY() + chapterScroll.getHeight()) {
                                isRequesting = true;
                                new Thread(reqChapter).start();//向后端请求更多书本
                            }
                        }
                }
                return false;
            }
        });
        loadingView = findViewById(R.id.Loading);

        //设置顶部状态栏样式
        Window window=getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0XB3371D4E);


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

        bookinfo.setVisibility(View.INVISIBLE);
        chapterScroll.setVisibility(View.INVISIBLE);
        introScroll.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);//加载画面
        findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
        findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

        LinearLayout tagsView = findViewById(R.id.tags);
        tagsView.removeAllViews();
        chapterTable.removeAllViews();
        chapters = new JSONArray();
        from = 0;

        new Thread(getBookInfo).start();

        if(account.length() != 0) {//length为0说明没有登陆，无需检验当前用户是否收藏了该书
            new Thread(checkFav).start();
        }
        new Thread(reqChapter).start();
    }

    //刷新界面
    public void refresh(View view){
        refresh();
    }

    public void switchToIntro(View view){
        if(WHICH==INTRO) return;

        WHICH=INTRO;
        Intro.setTextSize(19);

        Chapter.setTextSize(14);

        introScroll.setVisibility(View.VISIBLE);
        chapterScroll.setVisibility(View.GONE);
    }

    public void switchToChapter(View view){
        if(WHICH==CHAPTER) return;

        WHICH=CHAPTER;
        Chapter.setTextSize(19);

        Intro.setTextSize(14);

        introScroll.setVisibility(View.GONE);
        chapterScroll.setVisibility(View.VISIBLE);
    }

    public void isFav(View view){
        //如果用户未登录，跳转到登陆界面
        if(account.equals("")){
            Intent intent = new Intent(this,LogActivity.class);
            startActivity(intent);
            return;
        }


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

    private void toNewChapter(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.gender_edit_dialog,null);

        TextView title = view.findViewById(R.id.Title);
        title.setText(getResources().getString(R.string.mode));

        TextView tts = view.findViewById(R.id.Male);
        tts.setText(getResources().getString(R.string.tts));


        TextView stt = view.findViewById(R.id.Female);
        stt.setText(getResources().getString(R.string.stt));

        builder.setView(view);

        final AlertDialog dialog = builder.show();

        tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookActivity.this, NewChapterForTTSActivity.class);
                intent.putExtra("bookid",bookid);
                dialog.dismiss();
                startActivity(intent);
            }
        });


        stt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookActivity.this, NewChapterForSTTActivity.class);
                intent.putExtra("bookid",bookid);
                dialog.dismiss();
                startActivity(intent);
            }
        });
    }

    //开启管理模式
    private void toManage(){
        if(ismanaging) return;

        ismanaging = true;
        manageBox.setVisibility(View.VISIBLE);
        for(int i=0;i<chapters.length();i++){
            final View chapterRow = chapterTable.getChildAt(i);
            CheckBox checkBox = chapterRow.findViewById(R.id.checkBox);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    //退出管理
    public void cancelManage(View view){
        cancelManage();
    }

    private void cancelManage(){

        ismanaging = false;
        manageBox.setVisibility(View.GONE);
        for(int i=0;i<chapters.length();i++){
            final View chapterRow = chapterTable.getChildAt(i);
            CheckBox checkBox = chapterRow.findViewById(R.id.checkBox);
            checkBox.setChecked(false);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    //删除章节
    public void doDelete(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        builder.setTitle("删除章节");
        builder.setMessage("确认删除这些章节吗?");
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new MyToast(BookActivity.this,"成功删除!");

                final LinearLayout delete = findViewById(R.id.check);
                delete.setClickable(false);
                CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        delete.setClickable(true);
                    }
                };//防止用户高频率点击
                countDownTimer.start();

                new Thread(deleteChapter).start();
            }
        });
    }

    private void toChapter(int chapterID){

        /*if(ismanaging) {
            Intent intent = new Intent(this, EditChapterActivity.class);
            intent.putExtra("id", chapterID);
            startActivity(intent);
        }
        else {*/
            Intent intent = new Intent(this, ChapterActivity.class);
            intent.putExtra("chapterId", chapterID);
            intent.putExtra("bookid",bookid);

            //判断是否有权限修改章节
            if(author.equals(account)){
                intent.putExtra("hasPre",true);
            }
            startActivity(intent);
        //}
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
                    if(BookActivity.this.isFinishing()) return;
                    BookActivity.this.runOnUiThread(new Runnable() {
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
                    if(BookActivity.this.isFinishing()) return;
                    BookActivity.this.runOnUiThread(new Runnable() {
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
                    if(BookActivity.this.isFinishing()) return;
                    BookActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this,getResources().getString(R.string.HttpTimeOut));
                        }
                    });
                }

                else {
                    final String result = new String(outputStream.toByteArray(),
                            StandardCharsets.UTF_8);

                    if(BookActivity.this.isFinishing()) return;
                    BookActivity.this.runOnUiThread(new Runnable() {
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
                    if(BookActivity.this.isFinishing()) return;
                    BookActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject book = new JSONObject(result);

                if(BookActivity.this.isFinishing()) return;
                BookActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(book.getInt("id") == -1){//说明该作品已被删除
                                loadingView.setVisibility(View.INVISIBLE);
                                View deleted = LayoutInflater.from(BookActivity.this).inflate(R.layout.book_deleted, null);
                                BookActivity.this.setContentView(deleted);
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            else {
                                TextView Title = findViewById(R.id.title);
                                title = book.getString("name");
                                Title.setText(title);

                                TextView Author = findViewById(R.id.author);
                                author = book.getString("author");
                                Author.setText(author);

                                if(author.equals(account)){//如果本书的作者是当前账号，则提供管理章节的选项
                                    menu.setVisibility(View.VISIBLE);
                                }

                                TextView introduction = findViewById(R.id.introduction);
                                intro = book.getString("intro");
                                introduction.setText(intro);

                                LinearLayout tagsView = findViewById(R.id.tags);
                                String tagStr = book.getString("tags");
                                String[] tags = tagStr.split(" ");

                                for(int j=0;j<tags.length;j++){
                                    String tag = tags[j];
                                    TextView t = new TextView(BookActivity.this);
                                    t.setTextSize(10);
                                    if(isInNight){
                                        t.setTextColor(Color.WHITE);
                                    }else {
                                        t.setTextColor(Color.GRAY);
                                    }
                                    t.setText(tag);

                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.setMargins(0,15,0,0);
                                    t.setLayoutParams(layoutParams);
                                    tagsView.addView(t);
                                }

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

            if(BookActivity.this.isFinishing()) return;
            BookActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(surface != null){
                        ImageView Surface = findViewById(R.id.surface);
                        Surface.setImageBitmap(surface);
                    }

                    loadingView.setVisibility(View.INVISIBLE);
                    bookinfo.setVisibility(View.VISIBLE);
                    chapterScroll.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                }
            });
        }
    };

    Runnable getAvatar = new Runnable() {
        @Override
        public void run() {
            GetPicture getPicture = new GetPicture();
            final Bitmap avatar = getPicture.getAvatar(author);

            if(BookActivity.this.isFinishing()) return;
            BookActivity.this.runOnUiThread(new Runnable() {
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

    Runnable reqChapter = new Runnable() {
        @Override
        public void run() {
            if(BookActivity.this.isFinishing()) return;
            BookActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView text = pullDown.findViewById(R.id.content);
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
                    if(BookActivity.this.isFinishing()) return;
                    BookActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(BookActivity.this, getResources().getString(R.string.HttpTimeOut));
                            isRequesting = false;
                            TextView text = pullDown.findViewById(R.id.content);
                            text.setText(getResources().getString(R.string.pullDown));
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

                if(BookActivity.this.isFinishing()) return;
                BookActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chapterTable.removeView(pullDown);

                        for(int i=0;i < newChapters.length();i++){
                            try{
                                //System.out.println("new chapter");
                                final JSONObject chapter = newChapters.getJSONObject(i);

                                final View chapterRow;
                                if(isInNight){
                                    chapterRow = LayoutInflater.from(BookActivity.this).inflate(R.layout.chapter_row_night, null);
                                }else {
                                    chapterRow = LayoutInflater.from(BookActivity.this).inflate(R.layout.chapter_row, null);
                                }

                                TextView titleView = chapterRow.findViewById(R.id.chaptername);
                                titleView.setText(chapter.getString("title"));

                                TextView chapterNumber = chapterRow.findViewById(R.id.chapternumber);
                                chapterNumber.setText(String.valueOf(from + i + 1));

                                TextView timeView = chapterRow.findViewById(R.id.time);
                                timeView.setText(chapter.getString("length"));

                                if(ismanaging){
                                    CheckBox checkBox = chapterRow.findViewById(R.id.checkBox);
                                    checkBox.setVisibility(View.VISIBLE);
                                }
                                chapterTable.addView(chapterRow);
                                chapterRow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            if(ismanaging){
                                                CheckBox checkBox = chapterRow.findViewById(R.id.checkBox);
                                                checkBox.setChecked(!checkBox.isChecked());
                                            }else toChapter(chapter.getInt("id"));
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        chapterTable.addView(pullDown);
                        if(newChapters.length() < PAGESIZE){
                            TextView textView = pullDown.findViewById(R.id.content);
                            textView.setText(getResources().getString(R.string.hasEnd));
                            isRequesting = false;
                        }//说明章节已请求完毕
                        else {
                            TextView textView = pullDown.findViewById(R.id.content);
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

    Runnable deleteChapter = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/deletechapters";

                JSONArray ids = new JSONArray();//要删除章节的id
                JSONObject params = new JSONObject();
                params.put("bookid",bookid);

                final List<Integer> removes = new ArrayList<>();//存储要删除的书本在chapters中的index
                for (int i = 0; i < chapters.length(); i++) {
                    View bookRow = chapterTable.getChildAt(i);
                    CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                    if (checkBox.isChecked()) {
                        removes.add(i);
                        JSONObject object = new JSONObject();
                        object.put("id", chapters.getJSONObject(i).getInt("id"));
                        ids.put(object);
                    }
                }

                BookActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(BookActivity.this.isFinishing()) return;
                        int hasRemoved=0;
                        for (int i : removes) {
                            try {
                                chapterTable.removeViewAt(i - hasRemoved);
                                chapters.remove(i - hasRemoved);//在删除的时候，i要减去前面已经删除的书本数目
                                hasRemoved++;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        from = from - removes.size();//更新请求章节的index
                    }
                });

                params.put("ids",ids);
                byte[] param = params.toString().getBytes();
                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST", "application/json");//向后端发送删除请求

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
}
