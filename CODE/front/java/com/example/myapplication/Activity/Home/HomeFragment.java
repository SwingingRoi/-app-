package com.example.myapplication.Activity.Home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.Activity.Book.BookActivity;

import com.example.myapplication.Activity.Work.NewBookActivity;
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
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

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
    private String account;
    private boolean hasLogged;
    final private int PAGESIZE=10;
    private JSONArray books = new JSONArray();
    private List<Drawable> tag_border_styles;//标签边框样式
    private HashMap<String,Boolean> tagStats;//存储标签的选择状态
    private int count = 0;//已选择标签数

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

        tag_border_styles = new ArrayList<>();
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_red));
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_brown));
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_blue));

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

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if (!isRequesting && scrollView.getChildAt(0).getMeasuredHeight() <= scrollView.getScrollY() + scrollView.getHeight()) {
                        isRequesting = true;
                        new Thread(getBooks).start();//向后端请求更多书本
                    }
                }
                return false;
            }
        });

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)scrollView.getLayoutParams();
        params.height = (int) (height/1.28);
        scrollView.setLayoutParams(params);

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
        initTagStat();

        if(hasLogged){
            new Thread(checkPreference).start();
        }



        return view;
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
        normal.setVisibility(View.INVISIBLE);

        new Thread(getBooks).start();
    }

    private void setTagClickListener(final TextView tagView){
        try{
            tagView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String tag = tagView.getText().toString();
                    boolean oldStat = tagStats.get(tag);

                    //设置标签样式，更新已选择标签数
                    if(!oldStat){//选择标签
                        if(count == 3){
                            new MyToast(getActivity(), getResources().getString(R.string.remind));
                            return;
                        }

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
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.book_tags,null);

            TextView title = v.findViewById(R.id.title);
            title.setText(getResources().getString(R.string.choosepreference));

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
                    new Thread(storePreference).start();
                    dialog.dismiss();
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

    Runnable getBooks = new Runnable() {
        @Override
        public void run() {
            try{
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
                getActivity().runOnUiThread(new Runnable() {
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

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
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

                                TextView viewNumber = bookRow.findViewById(R.id.viewnumber);
                                viewNumber.setText(String.valueOf(newBooks.getJSONObject(i).getInt("views")));

                                TextView chapterNumber = bookRow.findViewById(R.id.chapternumber);
                                chapterNumber.setText(newBooks.getJSONObject(i).getInt("chapters") + "章");

                                LinearLayout tagsView = bookRow.findViewById(R.id.tags);
                                String tagStr = newBooks.getJSONObject(i).getString("tags");
                                String[] tags = tagStr.split(" ");

                                for(int j=0;j<tags.length;j++){
                                    String tag = tags[j];
                                    View tagView = LayoutInflater.from(getActivity()).inflate(R.layout.book_tag,null);
                                    TextView t = tagView.findViewById(R.id.tag);
                                    t.setText(tag);
                                    t.setBackground(tag_border_styles.get(j));

                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.setMargins(15,0,0,0);
                                    tagView.setLayoutParams(layoutParams);

                                    tagsView.addView(tagView);
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

                        loadView.setVisibility(View.INVISIBLE);
                        normal.setVisibility(View.VISIBLE);

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

    //检查用户的preference是否为空,为空则让用户选择感兴趣的标签
    Runnable checkPreference = new Runnable() {
        @Override
        public void run() {
            try{
                final GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getPreference?account=" + URLEncoder.encode(account,"UTF-8");

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");


                if (outputStream == null) {//请求超时
                    getActivity().runOnUiThread(new Runnable() {
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

                if(result.length() == 0){//说明数据库中用户的preference为空
                    //弹出让用户选择偏好标签的窗口
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chooseTags();
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //存储用户选择的标签
    Runnable storePreference = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/storePreference?account=" + URLEncoder.encode(account,"UTF-8");

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

                JSONObject params = new JSONObject();
                params.put("tags",tags);
                initTagStat();

                byte[] param = params.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);

                httpUtils.doHttp(param,"POST", "application/json");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
}
