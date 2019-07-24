package com.example.myapplication.Activity.Book;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
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

import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.Activity.Work.EditChapterActivity;
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
    private ScrollView introScroll;
    private ScrollView chapterScroll;
    private LinearLayout loadingView;
    private MySpinner menu;
    private LinearLayout manageBox;
    private View pullDown;//请求文字提示
    private List<Drawable> tag_border_styles;//标签边框样式

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
    private boolean hasClickMenu = false;//是否点击菜单
    private boolean ismanaging = false;//是否处于管理模式

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Intro = findViewById(R.id.intro);
        Chapter = findViewById(R.id.chapter);
        fav = findViewById(R.id.favicon);
        bookinfo = findViewById(R.id.bookinfo);

        tag_border_styles = new ArrayList<>();
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_red));
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_brown));
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_blue));

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
                if (select.equals("新建章节")) {
                    toNewChapter();
                }
                else {
                    if(!ismanaging){
                        toManage();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        manageBox.setVisibility(View.INVISIBLE);
        for(int i=0;i<chapters.length();i++){
            final View chapterRow = chapterTable.getChildAt(i);
            CheckBox checkBox = chapterRow.findViewById(R.id.checkBox);
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
        if(ismanaging) {
            Intent intent = new Intent(this, EditChapterActivity.class);
            intent.putExtra("id", chapterID);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, ChapterActivity.class);
            intent.putExtra("chapterId", chapterID);
            intent.putExtra("bookid",bookid);
            startActivity(intent);
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
                    BookActivity.this.runOnUiThread(new Runnable() {
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

                BookActivity.this.runOnUiThread(new Runnable() {
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
                                    View tagView = LayoutInflater.from(BookActivity.this).inflate(R.layout.book_tag,null);
                                    TextView t = tagView.findViewById(R.id.tag);
                                    t.setText(tag);
                                    t.setBackground(tag_border_styles.get(j));
                                    t.setTextColor(Color.WHITE);
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.setMargins(0,15,0,0);
                                    tagView.setLayoutParams(layoutParams);
                                    tagsView.addView(tagView);
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

                }
            });
        }
    };

    Runnable getAvatar = new Runnable() {
        @Override
        public void run() {
            GetPicture getPicture = new GetPicture();
            final Bitmap avatar = getPicture.getAvatar(author);

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

                BookActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chapterTable.removeView(pullDown);

                        for(int i=0;i < newChapters.length();i++){
                            try{
                                final JSONObject chapter = newChapters.getJSONObject(i);

                                View chapterRow = LayoutInflater.from(BookActivity.this).inflate(R.layout.chapter_row, null);
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
                                            toChapter(chapter.getInt("id"));
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
