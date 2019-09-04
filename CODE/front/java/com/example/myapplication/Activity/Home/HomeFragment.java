package com.example.myapplication.Activity.Home;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private LinearLayout normal;
    private LinearLayout loadView;
    private LinearLayout bookTable;
    private SwipeRefreshLayout swipeRefreshLayout;//下拉刷新控件
    private ScrollView scrollView;
    private View pullDown;//请求文字提示

    private boolean isRequesting = false;//当前是否在向后端请求书本信息
    private int from=0;//当前获取的works从第几本书开始
    private boolean firstIn = true;//是否是第一次进入该页面
    private String account;
    private boolean hasLogged;
    final private int PAGESIZE=10;
    private JSONArray books = new JSONArray();
    private HashMap<String,Boolean> tagStats;//存储标签的选择状态
    private boolean isInNight = false;//是否处于夜间模式
    private float preY;//用户触摸屏幕时的手指纵坐标
    private float nowY;//用户手指离开屏幕时的纵坐标

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        final ImageView searchIcon = view.findViewById(R.id.SearchIcon);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),SearchActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserState",MODE_PRIVATE);
        hasLogged = sharedPreferences.getBoolean("HasLogged",false);
        account = sharedPreferences.getString("Account","");

        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式


        if(isInNight) {
            TextView remind = view.findViewById(R.id.Remind);
            remind.setTextColor(Color.WHITE);
        }


        normal = view.findViewById(R.id.normal);
        bookTable = view.findViewById(R.id.BookTable);

        if(isInNight){
            pullDown = LayoutInflater.from(getActivity()).inflate(R.layout.pull_down_night, null);
        }else {
            pullDown = LayoutInflater.from(getActivity()).inflate(R.layout.pull_down, null);
        }
        bookTable.addView(pullDown);

        scrollView = view.findViewById(R.id.scroll);
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
                                new Thread(getBooks).start();//向后端请求更多书本
                            }
                        }
                }
                return false;
            }
        });

        //根据屏幕大小调节控件的尺寸
        /*DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)swipeRefreshLayout.getLayoutParams();
        params.height = (int) (height/1.28);
        swipeRefreshLayout.setLayoutParams(params);*/

        swipeRefreshLayout = view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        loadView = view.findViewById(R.id.Loading);
        loadView.setVisibility(View.VISIBLE);
        loadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        normal.setVisibility(View.INVISIBLE);
        refresh();

        tagStats = new HashMap<>();


        return view;
    }


    public void jumpToBook(int bookid){
        Intent intent = new Intent(getActivity(),BookActivity.class);
        intent.putExtra("id",bookid);
        startActivity(intent);
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
        bookTable.removeAllViews();
        books = new JSONArray();


        loadView.setVisibility(View.VISIBLE);//加载画面
        loadView.findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
        loadView.findViewById(R.id.Remind).setVisibility(View.INVISIBLE);
        normal.setVisibility(View.INVISIBLE);

        new Thread(getBooks).start();
    }

    Runnable getBooks = new Runnable() {
        @Override
        public void run() {
        try{
            if(getActivity() == null) return;
            if(getActivity().isFinishing()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView text = pullDown.findViewById(R.id.content);
                    text.setText(getResources().getString(R.string.isReq));
                }
            });



            final GetServer getServer = new GetServer();
            String url = getServer.getIPADDRESS()+"/audiobook/getbooks?from=" + from + "&size=" + PAGESIZE;

            HttpUtils httpUtils = new HttpUtils(url);
            ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                    "application/json");


            if (outputStream == null) {//请求超时
                if(getActivity() == null) return;
                if(getActivity().isFinishing()) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(HomeFragment.this.getActivity(), getResources().getString(R.string.HttpTimeOut));
                        isRequesting = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if(firstIn) {
                            loadView.setVisibility(View.VISIBLE);
                            loadView.findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            loadView.findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    }
                });
                return;
            }

            firstIn = false;

            final String result = new String(outputStream.toByteArray(),
                    StandardCharsets.UTF_8);

            final JSONArray newBooks = new JSONArray(result);


            for (int i = 0; i < newBooks.length(); i++) {
                books.put(newBooks.getJSONObject(i));
            }//向works中添加新请求过来的work

            if(getActivity() == null) return;
            if(getActivity().isFinishing()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bookTable.removeView(pullDown);
                        loadView.setVisibility(View.INVISIBLE);
                        swipeRefreshLayout.setRefreshing(false);//隐藏刷新动画
                        normal.setVisibility(View.VISIBLE);

                        if(books.length() == 0){
                            View nobookView;
                            if(isInNight) nobookView = LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.no_book_style_night, null);
                            else nobookView = LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.no_book_style, null);
                            bookTable.removeAllViews();
                            bookTable.addView(nobookView);
                            isRequesting = false;
                            return;
                        }

                        for (int i = 0; i < newBooks.length(); i++) {
                            try {
                                View bookRow;
                                if(isInNight){
                                    bookRow = LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.book_row_style_night, null);
                                }else {
                                    bookRow = LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.book_row_style, null);
                                }

                                TextView title = bookRow.findViewById(R.id.BookName);
                                title.setText(newBooks.getJSONObject(i).getString("name"));

                                TextView viewNumber = bookRow.findViewById(R.id.viewnumber);
                                viewNumber.setText(String.valueOf(newBooks.getJSONObject(i).getInt("views")));

                                TextView chapterNumber = bookRow.findViewById(R.id.chapternumber);
                                chapterNumber.setText(newBooks.getJSONObject(i).getInt("chapters") + "章");

                                LinearLayout tagsView = bookRow.findViewById(R.id.tags);
                                String tagStr = newBooks.getJSONObject(i).getString("tags");
                                String[] tags = tagStr.split(" ");

                                for(int j=0;j<tags.length;j++){
                                    String tag = tags[j];
                                    TextView t = new TextView(getActivity());
                                    t.setText(tag);
                                    t.setTextSize(10);
                                    if(isInNight){
                                        t.setTextColor(Color.WHITE);
                                    }else {
                                        t.setTextColor(Color.GRAY);
                                    }

                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.setMargins(15,0,0,0);
                                    t.setLayoutParams(layoutParams);

                                    tagsView.addView(t);
                                }

                                bookTable.addView(bookRow);

                                final int id = newBooks.getJSONObject(i).getInt("id");
                                bookRow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        jumpToBook(id);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        bookTable.addView(pullDown);

                        if(newBooks.length() < PAGESIZE){
                            TextView textView = pullDown.findViewById(R.id.content);
                            textView.setText(getResources().getString(R.string.hasEnd));
                            isRequesting = false;
                        }//说明书本已请求完毕
                        else {
                            TextView textView = pullDown.findViewById(R.id.content);
                            textView.setText(getResources().getString(R.string.pullDown));
                            isRequesting = false;
                        }

                        for (int i = from; i < books.length(); i++) {
                            Thread thread = new GetSurface(i);
                            thread.start();
                        }

                        from = from + newBooks.length();//更新请求index
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
                final Bitmap surface = getPicture.getSurface(books.getJSONObject(index).getInt("id"));

                if(getActivity() == null) return;
                if(getActivity().isFinishing()) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (surface != null) {
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


}
