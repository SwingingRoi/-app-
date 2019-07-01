package com.example.myapplication.Activity.Home;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.Activity.Work.EditBookActivity;
import com.example.myapplication.Activity.Work.NewBookActivity;
import com.example.myapplication.Activity.Work.PersonalWorkActivity;
import com.example.myapplication.BookActivity;

import com.example.myapplication.GetServer;
import com.example.myapplication.HttpUtils;
import com.example.myapplication.MyToast;
import com.example.myapplication.PicUtils.GetPicture;
import com.example.myapplication.R;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HomeFragment extends Fragment {

    private LinearLayout normal;
    private LinearLayout loadView;
    private LinearLayout bookTable;
    private ImageView Refresh;
    private ScrollView scrollView;
    private View pullDown;//请求文字提示

    private boolean isRequesting = false;//当前是否在向后端请求书本信息
    private int from=0;//当前获取的works从第几本书开始
    private boolean firstIn = true;//是否是第一次进入该页面
    final private int PAGESIZE=10;
    private JSONArray books = new JSONArray();

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


        normal = view.findViewById(R.id.normal);
        bookTable = view.findViewById(R.id.BookTable);

        Refresh = view.findViewById(R.id.refresh);
        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        pullDown = LayoutInflater.from(getActivity()).inflate(R.layout.pull_down, null);
        bookTable.addView(pullDown);

        scrollView = view.findViewById(R.id.scroll);
        scrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isRequesting) return true;//如果正在请求新书，忽略滑动请求，防止发出重复的书本请求

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(isRequesting) return true;
                    if (scrollView.getChildAt(0).getMeasuredHeight() <= scrollView.getScrollY() + scrollView.getHeight()) {
                        isRequesting = true;
                        new Thread(getBooks).start();//向后端请求更多书本
                    }
                }
                return false;
            }
        });

        loadView = view.findViewById(R.id.Loading);
        loadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        normal.setVisibility(View.INVISIBLE);

        refresh();
        return view;
    }

    public void jumpToBook(int bookid){
        Intent intent = new Intent(getActivity(),BookActivity.class);
        intent.putExtra("id",bookid);
        startActivity(intent);
    }

    public void refresh(){

        Refresh.setClickable(false);
        loadView.setClickable(false);

        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                loadView.setClickable(true);
                Refresh.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        if (isRequesting) return;

        isRequesting = true;

        if (!firstIn) {
            from = 0;
            firstIn = true;
            bookTable.removeAllViews();
            books = new JSONArray();
        }

        loadView.setVisibility(View.VISIBLE);//加载画面
        loadView.findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
        loadView.findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

        new Thread(getBooks).start();
    }


    Runnable getBooks = new Runnable() {
        @Override
        public void run() {
            normal.post(new Runnable() {
                @Override
                public void run() {
                    TextView text = pullDown.findViewById(R.id.text);
                    text.setText(getResources().getString(R.string.isReq));
                }
            });

            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getbooks?from=" + from + "&size=" + PAGESIZE;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(HomeFragment.this.getActivity(), getResources().getString(R.string.HttpTimeOut));
                            isRequesting = false;

                            loadView.findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            if (firstIn) {//如果是首次进入，设置点击屏幕刷新提醒
                                loadView.findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONArray newBooks = new JSONArray(result);

                for (int i = 0; i < newBooks.length(); i++) {
                    books.put(newBooks.getJSONObject(i));
                }//向works中添加新请求过来的work

                normal.post(new Runnable() {
                    @Override
                    public void run() {

                        bookTable.removeView(pullDown);

                        if (firstIn) {//如果是首次进入该activity
                            firstIn = false;

                            if (books.length() == 0) {//说明数据库中没有书本
                                View nobookView = LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.no_book_style, null);
                                bookTable.addView(nobookView);
                                isRequesting = false;
                                return;
                            }
                        }

                        for (int i = 0; i < newBooks.length(); i++) {
                            try {
                                View bookRow = LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.book_row_style, null);
                                TextView title = bookRow.findViewById(R.id.BookName);
                                title.setText(newBooks.getJSONObject(i).getString("name"));
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
                            TextView textView = pullDown.findViewById(R.id.text);
                            textView.setText(getResources().getString(R.string.hasEnd));
                            isRequesting = false;
                        }//说明书本已请求完毕
                        else {
                            TextView textView = pullDown.findViewById(R.id.text);
                            textView.setText(getResources().getString(R.string.pullDown));
                            isRequesting = false;
                        }

                        for (int i = from; i < books.length(); i++) {
                            Thread thread = new GetSurface(i);
                            thread.start();
                        }

                        from = from + newBooks.length();//更新请求index

                        normal.post(new Runnable() {
                            @Override
                            public void run() {
                                loadView.setVisibility(View.INVISIBLE);
                                normal.setVisibility(View.VISIBLE);
                            }
                        });

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

                normal.post(new Runnable() {
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
}
