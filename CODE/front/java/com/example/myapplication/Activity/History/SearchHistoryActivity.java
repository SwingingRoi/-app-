package com.example.myapplication.Activity.History;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.Activity.Book.BookActivity;
import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.InternetUtils.HttpUtils;
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

public class SearchHistoryActivity extends AppCompatActivity {

    private LinearLayout bookTable;
    private LinearLayout manageBox;
    private LinearLayout normal;
    private LinearLayout loadView;
    private ScrollView scrollView;
    private View pullDown;//请求文字提示
    private JSONArray searchresults;
    private boolean isRequesting = true;//当前是否在向后端请求书本信息
    private boolean ismanaging = false;//是否处于管理模式
    final private int SEARCHBTN = 1;
    final private int SCROLL = 2;
    private int SEARCHREQ = SEARCHBTN;
    private boolean isInNight = false;//是否处于夜间模式
    private float preY;//用户触摸屏幕时的手指纵坐标
    private float nowY;//用户手指离开屏幕时的纵坐标


    final private int TODAY=0;
    final private int YESTERDAY=1;
    final private int TWODAYAGO=2;
    final private int FORMER=3;
    private int NOWCHOICE=TODAY;
    private String account;
    private int from = 0;
    private int PAGESIZE=20;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("UserState", MODE_PRIVATE);
        account = sharedPreferences.getString("Account", "");
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_search_history_night);
        }else {
            setContentView(R.layout.activity_search_history);
        }

        bookTable = findViewById(R.id.BookTable);
        manageBox = findViewById(R.id.manage);
        normal = findViewById(R.id.normal);
        loadView = findViewById(R.id.Loading);
        if(isInNight){
            pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down_night, null);
        }else {
            pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down, null);
        }


        scrollView = findViewById(R.id.scrollView);
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
                                SEARCHREQ = SCROLL;
                                new Thread(search).start();//向后端请求更多书本
                            }
                        }
                }
                return false;
            }
        });

        searchresults = new JSONArray();
    }

    public void onBackPressed(View view){
        super.onBackPressed();
    }

    //弹出时间选择窗口
    public void chooseDay(View view){
        final String[] dayChoice = new String[]{"今天","昨天","前天","更早"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        builder.setTitle("选择时间");

        final TextView dayView = findViewById(R.id.day);
        final int formerChoice = NOWCHOICE;
        //设置选择监听事件
        builder.setSingleChoiceItems(dayChoice, NOWCHOICE,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NOWCHOICE = which;
                    }
                });

        final AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dayView.setText(dayChoice[NOWCHOICE]);
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                NOWCHOICE = formerChoice;
            }
        });
    }

    private void jumpToBook(int bookid){
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra("id",bookid);
        startActivity(intent);
    }

    //开启管理模式
    public void toManage(View view){
        if(ismanaging) return;

        ismanaging = true;
        manageBox.setVisibility(View.VISIBLE);
        for(int i=0;i<searchresults.length();i++){
            final View bookRow = bookTable.getChildAt(i);
            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    //退出管理
    public void cancelManage(View view){

        ismanaging = false;
        manageBox.setVisibility(View.GONE);
        for(int i=0;i<searchresults.length();i++){
            final View bookRow = bookTable.getChildAt(i);
            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
            checkBox.setChecked(false);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    //搜索
    public void searchHistory(View view){

        loadView.setVisibility(View.VISIBLE);//加载画面

        final TextView searchBtn = findViewById(R.id.SearchBtn);
        searchBtn.setClickable(false);
        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                searchBtn.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        searchresults = new JSONArray();
        bookTable.removeAllViews();
        from = 0;//清空上一次的搜索结果
        SEARCHREQ = SEARCHBTN;
        isRequesting = true;
        ismanaging = false;
        new Thread(search).start();
    }

    Runnable search = new Runnable() {
        @Override
        public void run() {
            try{
                if(SearchHistoryActivity.this.isFinishing()) return;
                SearchHistoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView text = pullDown.findViewById(R.id.content);
                        text.setText(getResources().getString(R.string.isReq));
                    }
                });

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/searchHistory?account=" + URLEncoder.encode(account,"UTF-8")
                        + "&day=" + NOWCHOICE + "&from=" + from + "&size=" + PAGESIZE;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if(outputStream == null){
                    if(SearchHistoryActivity.this.isFinishing()) return;
                    SearchHistoryActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(SearchHistoryActivity.this, getResources().getString(R.string.HttpTimeOut));
                            loadView.setVisibility(View.INVISIBLE);
                            bookTable.removeAllViews();
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONArray resultArray = new JSONArray(result);

                for(int i=0;i<resultArray.length();i++){
                    searchresults.put(resultArray.getJSONObject(i));
                }

                if(SearchHistoryActivity.this.isFinishing()) return;
                SearchHistoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if(SEARCHREQ == SCROLL) {
                                bookTable.removeView(pullDown);
                            }

                            if (searchresults.length() == 0) {//搜索结果为空
                                SearchHistoryActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadView.setVisibility(View.INVISIBLE);
                                        View noresult;
                                        if(isInNight) noresult = LayoutInflater.from(SearchHistoryActivity.this).inflate(R.layout.search_no_result_night, null);
                                        else noresult = LayoutInflater.from(SearchHistoryActivity.this).inflate(R.layout.search_no_result, null);
                                        bookTable.removeAllViews();
                                        bookTable.addView(noresult);
                                    }
                                });
                                return;
                            }

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject record = resultArray.getJSONObject(i);
                                final View bookRow;
                                if(isInNight){
                                    bookRow = LayoutInflater.from(SearchHistoryActivity.this).inflate(R.layout.history_row_night, null);
                                }else {
                                    bookRow = LayoutInflater.from(SearchHistoryActivity.this).inflate(R.layout.history_row, null);
                                }

                                TextView title = bookRow.findViewById(R.id.BookName);
                                title.setText(record.getString("name"));

                                TextView timeView = bookRow.findViewById(R.id.time);
                                timeView.setText(record.getString("time"));

                                LinearLayout tagsView = bookRow.findViewById(R.id.tags);
                                String tagStr = resultArray.getJSONObject(i).getString("tags");
                                String[] tags = tagStr.split(" ");

                                for(int j=0;j<tags.length;j++){
                                    String tag = tags[j];
                                    TextView t = new TextView(SearchHistoryActivity.this);
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

                                final int id = resultArray.getJSONObject(i).getInt("bookid");
                                bookRow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(ismanaging){
                                            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                                            checkBox.setChecked(!checkBox.isChecked());
                                        }else jumpToBook(id);
                                    }
                                });
                            }//将搜索结果添加至bookTable中


                            bookTable.addView(pullDown);
                            if (resultArray.length() < PAGESIZE) {
                                TextView textView = pullDown.findViewById(R.id.content);
                                textView.setText(getResources().getString(R.string.hasEnd));
                                isRequesting = false;
                            }//说明书本已请求完毕
                            else {
                                TextView textView = pullDown.findViewById(R.id.content);
                                textView.setText(getResources().getString(R.string.pullDown));
                                isRequesting = false;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                //获取封面
                for (int i = from; i < searchresults.length(); i++) {
                    Thread thread = new GetSurface(i);
                    thread.start();
                }

                from = from + resultArray.length();//更新请求index

                SearchHistoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadView.setVisibility(View.INVISIBLE);
                        normal.setVisibility(View.VISIBLE);
                    }
                });

            }catch (Exception e){
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
                final Bitmap surface = getPicture.getSurface(searchresults.getJSONObject(index).getInt("bookid"));

                if(SearchHistoryActivity.this.isFinishing()) return;
                SearchHistoryActivity.this.runOnUiThread(new Runnable() {
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

    public void deleteHistory(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        builder.setTitle("删除历史记录");
        builder.setMessage("确认删除记录吗?");
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new MyToast(SearchHistoryActivity.this,"已删除!");

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

    Runnable deleteHistory = new Runnable() {
        @Override
        public void run() {
              try{

                  GetServer getServer = new GetServer();
                  String url = getServer.getIPADDRESS()+"/audiobook/deleteHistory";

                  JSONArray ids = new JSONArray();//要删除的记录id

                  final List<Integer> removes = new ArrayList<>();//存储要删除的记录在searchresults中的index
                  for (int i = 0; i < searchresults.length(); i++) {
                      View bookRow = bookTable.getChildAt(i);
                      CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                      if (checkBox.isChecked()) {
                          removes.add(i);
                          JSONObject object = new JSONObject();
                          object.put("id", searchresults.getJSONObject(i).getInt("id"));
                          ids.put(object);
                      }
                  }

                  SearchHistoryActivity.this.runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          if(SearchHistoryActivity.this.isFinishing()) return;
                          int hasRemoved=0;
                          for (int i : removes) {
                              try {
                                  bookTable.removeViewAt(i - hasRemoved);
                                  searchresults.remove(i - hasRemoved);//在删除的时候，i要减去前面已经删除的书本数目
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
}
