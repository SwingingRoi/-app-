package com.example.myapplication.Activity.History;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.Activity.Book.BookActivity;
import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.InternetUtils.HttpUtils;
import com.example.myapplication.PicUtils.GetPicture;
import com.example.myapplication.R;
import com.example.myapplication.MyComponent.MyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private LinearLayout normal;
    private LinearLayout loadView;
    private LinearLayout bookTable;
    private LinearLayout manageBox;
    private ScrollView scrollView;
    private SwipeRefreshLayout swipeRefreshLayout;//下拉刷新控件
    private View pullDown;//请求文字提示
    private boolean firstIn = true;//是否是第一次进入该页面
    private boolean ismanaging = false;//是否处于管理模式
    private boolean isRequesting = false;//当前是否在向后端请求书本信息
    private boolean isInNight = false;//是否处于夜间模式
    private float preY;//用户触摸屏幕时的手指纵坐标
    private float nowY;//用户手指离开屏幕时的纵坐标

    private JSONArray historyArray;//存储历史记录的数组

    private int from=0;//当前获取的浏览记录index
    final private int PAGESIZE=20;//每次获取的记录数

    private String account;//当前账号

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserState", MODE_PRIVATE);
        account = sharedPreferences.getString("Account", "");
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_history_night);
        }else {
            setContentView(R.layout.activity_history);
        }

        historyArray = new JSONArray();

        bookTable = findViewById(R.id.BookTable);

        if(isInNight){
            pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down_night, null);
        }else {
            pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down, null);
        }

        bookTable.addView(pullDown);

        scrollView = findViewById(R.id.scroll);
        scrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        preY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        nowY = event.getY();
                        if(nowY < preY){
                            if (!isRequesting && scrollView.getChildAt(0).getMeasuredHeight() <= scrollView.getScrollY() + scrollView.getHeight()) {
                                isRequesting = true;
                                new Thread(getHistory).start();//向后端请求更多书本
                            }
                        }
                }
                return false;
            }
        });

        swipeRefreshLayout =findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        manageBox = findViewById(R.id.manage);

        normal = findViewById(R.id.Normal);
        loadView = findViewById(R.id.Loading);
        normal.setVisibility(View.INVISIBLE);

        refresh();
    }


    public void onBackPressed(View view){
        super.onBackPressed();
    }

    public void toHistorySearch(View view){
        Intent intent = new Intent(this,SearchHistoryActivity.class);
        startActivity(intent);
    }

    //刷新界面
    public void refresh(View view){
        refresh();
    }

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

        if (isRequesting) return;

        isRequesting = true;

        from = 0;
        firstIn = true;
        if(ismanaging) cancelManage();
        ismanaging = false;
        bookTable.removeAllViews();
        historyArray = new JSONArray();

        normal.setVisibility(View.INVISIBLE);
        loadView.setVisibility(View.VISIBLE);//加载画面
        findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
        findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

        new Thread(getHistory).start();
    }

    public void toManage(View view){
        toManage();
    }

    //开启管理模式
    public void toManage(){
        if(ismanaging) return;

        ismanaging = true;
        manageBox.setVisibility(View.VISIBLE);
        for(int i=0;i<historyArray.length();i++){
            View bookRow = bookTable.getChildAt(i);
            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
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
        for(int i=0;i<historyArray.length();i++){
            View bookRow = bookTable.getChildAt(i);
            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
            checkBox.setChecked(false);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    //删除历史记录
    public void deleteHistory(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        builder.setTitle("删除历史");
        builder.setMessage("确认删除吗?");
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new MyToast(HistoryActivity.this,"已删除!");

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

                new Thread(deleteHistory).start();
            }
        });
    }

    //清空历史记录
    public void clearHistory(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        builder.setTitle("清空历史");
        builder.setMessage("确认清空吗?");
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new MyToast(HistoryActivity.this,"已清空!");
                historyArray = new JSONArray();
                bookTable.removeAllViews();
                from = 0;//更新请求书本的index

                final LinearLayout clear = findViewById(R.id.clear);
                clear.setClickable(false);
                CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        clear.setClickable(true);
                    }
                };//防止用户高频率点击
                countDownTimer.start();

                new Thread(clearHistory).start();
            }
        });
    }

    //获取浏览记录
    Runnable getHistory = new Runnable() {
        @Override
        public void run() {
            if(HistoryActivity.this.isFinishing()) return;
            HistoryActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView text = pullDown.findViewById(R.id.content);
                    text.setText(getResources().getString(R.string.isReq));
                }
            });

            try {

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getHistory?account="
                        + URLEncoder.encode(account,"UTF-8") + "&from=" + from + "&size=" + PAGESIZE;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    if(HistoryActivity.this.isFinishing()) return;
                    HistoryActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(HistoryActivity.this, getResources().getString(R.string.HttpTimeOut));
                            isRequesting = false;
                            swipeRefreshLayout.setRefreshing(false);

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            if (firstIn) {//如果是首次进入，设置点击屏幕刷新提醒
                                findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    return;
                }


                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);
                /*result的形式:[{'bookid':bookid,'name':booktitle,'id':recordid,'time':time}...]
                */

                final JSONArray newRecords = new JSONArray(result);

                for (int i = 0; i < newRecords.length(); i++) {
                        historyArray.put(newRecords.getJSONObject(i));
                }//向historyArray中添加新请求过来的records

                if(HistoryActivity.this.isFinishing()) return;
                HistoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bookTable.removeView(pullDown);
                        loadView.setVisibility(View.INVISIBLE);
                        normal.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);

                        if (historyArray.length() == 0) {//说明该用户没有历史记录
                                View nohisView;
                                if(isInNight) nohisView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.no_history_style_night, null);
                                else nohisView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.no_history_style, null);
                                if(firstIn) {
                                    firstIn = false;
                                    bookTable.addView(nohisView);
                                }

                                isRequesting = false;
                                return;
                        }

                        //依次添加作品到bookTable中
                        try {
                            for (int i = 0; i < newRecords.length(); i++) {
                                JSONObject record = newRecords.getJSONObject(i);

                                final View bookRow;
                                if(isInNight){
                                    bookRow = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.history_row_night, null);
                                }else {
                                    bookRow = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.history_row, null);
                                }


                                TextView title = bookRow.findViewById(R.id.BookName);
                                title.setText(record.getString("name"));

                                TextView timeView = bookRow.findViewById(R.id.time);
                                timeView.setText(record.getString("time"));

                                LinearLayout tagsView = bookRow.findViewById(R.id.tags);
                                String tagStr = newRecords.getJSONObject(i).getString("tags");
                                String[] tags = tagStr.split(" ");

                                for(int j=0;j<tags.length;j++){
                                    String tag = tags[j];
                                    TextView t = new TextView(HistoryActivity.this);
                                    t.setTextSize(10);
                                    if(isInNight){
                                        t.setTextColor(Color.WHITE);
                                    }else {
                                        t.setTextColor(Color.GRAY);
                                    }
                                    t.setText(tag);

                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.setMargins(15,0,0,0);
                                    t.setLayoutParams(layoutParams);
                                    tagsView.addView(t);
                                }

                                if(ismanaging){
                                    CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                                    checkBox.setVisibility(View.VISIBLE);
                                }
                                bookTable.addView(bookRow);

                                final int id = record.getInt("bookid");
                                bookRow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(ismanaging){
                                            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                                            checkBox.setChecked(!checkBox.isChecked());
                                        }else jumpToBook(id);
                                    }
                                });
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(ismanaging){
                            toManage();
                        }

                        bookTable.addView(pullDown);
                        if(newRecords.length() < PAGESIZE){
                            TextView textView = pullDown.findViewById(R.id.content);
                            textView.setText(getResources().getString(R.string.hasEnd));
                            isRequesting = false;
                        }//说明书本已请求完毕
                        else {
                            TextView textView = pullDown.findViewById(R.id.content);
                            textView.setText(getResources().getString(R.string.pullDown));
                            isRequesting = false;
                        }
                    }
                });

                //获取封面
                for (int i = from; i < historyArray.length(); i++) {
                    Thread thread = new GetSurface(i);
                    thread.start();
                }

                from = from + newRecords.length();//更新请求index
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private class GetSurface extends Thread
    {
        private int index;

        public GetSurface(int index){
            this.index = index;
        }

        @Override
        public void run() {
            try {
                GetPicture getPicture = new GetPicture();
                final Bitmap surface = getPicture.getSurface(historyArray.getJSONObject(index).getInt("bookid"));

                if(HistoryActivity.this.isFinishing()) return;
                HistoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(surface!=null) {
                            View bookRow = bookTable.getChildAt(index);
                            ImageView surfaceView = bookRow.findViewById(R.id.surface);
                            surfaceView.setImageBitmap(surface);
                        }
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void jumpToBook(int bookid){
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra("id",bookid);
        startActivity(intent);
    }

    Runnable deleteHistory = new Runnable() {
        @Override
        public void run() {

            try {

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/deleteHistory";

                JSONArray ids = new JSONArray();//要删除的记录的id

                final List<Integer> removes = new ArrayList<>();//存储要删除的书本在works中的index
                for (int i = 0; i < historyArray.length(); i++) {
                    View bookRow = bookTable.getChildAt(i);
                    CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                    if (checkBox.isChecked()) {
                        removes.add(i);
                        JSONObject object = new JSONObject();
                        object.put("id", historyArray.getJSONObject(i).getInt("id"));
                        ids.put(object);
                    }
                }

                HistoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(HistoryActivity.this.isFinishing()) return;
                        int hasRemoved=0;
                        for (int i : removes) {
                            try {
                                bookTable.removeViewAt(i - hasRemoved);
                                historyArray.remove(i - hasRemoved);//在删除的时候，i要减去前面已经删除的书本数目
                                hasRemoved++;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        from = from - removes.size();//更新请求书本的index
                    }
                });

                byte[] param = ids.toString().getBytes();
                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST", "application/json");//向后端发送删除请求

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    };

    Runnable clearHistory = new Runnable() {
        @Override
        public void run() {

            try {
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/clearHistory?account=" + URLEncoder.encode(account,"UTF-8");

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(null, "POST", "application/json");//向后端发送删除请求
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    };
}
