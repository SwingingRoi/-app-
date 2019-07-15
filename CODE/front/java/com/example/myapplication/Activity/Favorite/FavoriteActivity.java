package com.example.myapplication.Activity.Favorite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
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

import com.example.myapplication.BookActivity;
import com.example.myapplication.GetServer;
import com.example.myapplication.HttpUtils;
import com.example.myapplication.MyToast;
import com.example.myapplication.PicUtils.GetPicture;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private LinearLayout normal;
    private LinearLayout loadView;
    private LinearLayout bookTable;
    private LinearLayout manageBox;
    private ScrollView scrollView;
    private ImageView refresh;
    private View pullDown;//请求文字提示
    private boolean firstIn = true;//是否是第一次进入该页面
    private boolean ismanaging = false;//是否处于管理模式
    private boolean isRequesting = false;//当前是否在向后端请求书本信息

    private JSONArray books;

    private int from=0;//当前获取的works从第几本书开始
    final private int PAGESIZE=10;

    private String account;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);


        SharedPreferences sharedPreferences = getSharedPreferences("UserState", MODE_PRIVATE);
        account = sharedPreferences.getString("Account", "");

        books = new JSONArray();

        refresh = findViewById(R.id.refresh);
        bookTable = findViewById(R.id.BookTable);
        pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down, null);
        bookTable.addView(pullDown);

        scrollView = findViewById(R.id.scroll);
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

        manageBox = findViewById(R.id.manage);

        normal = findViewById(R.id.Normal);
        loadView = findViewById(R.id.Loading);
        normal.setVisibility(View.INVISIBLE);

        refresh();

    }


    public void onBackPressed(View view){
        super.onBackPressed();
    }

    public void refresh(){

        refresh.setClickable(false);
        loadView.setClickable(false);

        CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                loadView.setClickable(true);
                refresh.setClickable(true);
            }
        };//防止用户高频率点击
        countDownTimer.start();

        if (isRequesting) return;

        isRequesting = true;

        if (!firstIn) {
            from = 0;
            firstIn = true;
            if(ismanaging) cancelManage();
            ismanaging = false;
            bookTable.removeAllViews();
            books = new JSONArray();
        }

        loadView.setVisibility(View.VISIBLE);//加载画面
        findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
        findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

        new Thread(getBooks).start();
    }

    //刷新界面
    public void refresh(View view){
        refresh();
    }

    public void toManage(View view){
        toManage();
    }

    //开启管理模式
    public void toManage(){
        if(ismanaging) return;

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scrollView.getLayoutParams();
        params.height = 1400;
        scrollView.setLayoutParams(params);//设置scrollView的高度

        ismanaging = true;
        manageBox.setVisibility(View.VISIBLE);
        for(int i=0;i<books.length();i++){
            final View bookRow = bookTable.getChildAt(i);
            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    //退出管理
    public void cancelManage(View view){
        cancelManage();
    }

    private void cancelManage(){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scrollView.getLayoutParams();
        params.height = 1600;
        scrollView.setLayoutParams(params);//设置scrollView的高度

        ismanaging = false;
        manageBox.setVisibility(View.INVISIBLE);
        for(int i=0;i < books.length();i++){
            final View bookRow = bookTable.getChildAt(i);
            CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    //删除图书
    public void cancelFav(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定",null);
        builder.setNegativeButton("取消",null);
        builder.setTitle("删除图书");
        builder.setMessage("确认取消收藏吗?");
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new MyToast(FavoriteActivity.this,"已取消收藏!");

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

                new Thread(cancelFav).start();
            }
        });
    }

    Runnable cancelFav = new Runnable() {
        @Override
        public void run() {

            try {

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/cancelFavorite?account=" + URLEncoder.encode(account,"UTF-8");

                JSONArray ids = new JSONArray();//要取消收藏书本的id

                final List<Integer> removes = new ArrayList<>();//存储要删除的书本在works中的index
                for (int i = 0; i < books.length(); i++) {
                    View bookRow = bookTable.getChildAt(i);
                    CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                    if (checkBox.isChecked()) {
                        removes.add(i);
                        JSONObject object = new JSONObject();
                        object.put("id", books.getJSONObject(i).getInt("id"));
                        ids.put(object);
                    }
                }


                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        int hasRemoved=0;
                        for (int i : removes) {
                            try {
                                bookTable.removeViewAt(i - hasRemoved);
                                books.remove(i - hasRemoved);//在删除的时候，i要减去前面已经删除的书本数目
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


    private void jumpToBook(int bookid){
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra("id",bookid);
        startActivity(intent);
    }

    public void toSearch(View view){
        Intent intent = new Intent(this,SearchFavoriteActivity.class);
        startActivity(intent);
    }

    //获取已有收藏
    Runnable getBooks = new Runnable() {
        @Override
        public void run() {
            normal.post(new Runnable() {
                @Override
                public void run() {
                    TextView text = pullDown.findViewById(R.id.content);
                    text.setText(getResources().getString(R.string.isReq));
                }
            });

            try {

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getFavorites?account="
                        + URLEncoder.encode(account,"UTF-8") + "&from=" + from + "&size=" + PAGESIZE;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(FavoriteActivity.this, getResources().getString(R.string.HttpTimeOut));
                            isRequesting = false;

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

                final JSONArray newBooks = new JSONArray(result);

                for (int i = 0; i < newBooks.length(); i++) {
                    books.put(newBooks.getJSONObject(i));
                }//向works中添加新请求过来的work

                normal.post(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        bookTable.removeView(pullDown);

                        if (books.length() == 0) {//说明该用户尚没有发布作品
                                View nofavView = LayoutInflater.from(FavoriteActivity.this).inflate(R.layout.no_favorite_style, null);
                                if(firstIn){
                                    firstIn = false;
                                    bookTable.addView(nofavView);
                                }

                                isRequesting = false;
                                return;
                        }

                        //依次添加作品到bookTable中
                        for (int i = 0; i < newBooks.length(); i++) {
                            try {
                                View bookRow = LayoutInflater.from(FavoriteActivity.this).inflate(R.layout.book_row_style, null);

                                TextView title = bookRow.findViewById(R.id.BookName);
                                title.setText(newBooks.getJSONObject(i).getString("name"));

                                TextView viewNumber = bookRow.findViewById(R.id.viewnumber);
                                viewNumber.setText(String.valueOf(newBooks.getJSONObject(i).getInt("views")));

                                TextView chapterNumber = bookRow.findViewById(R.id.chapternumber);
                                chapterNumber.setText(newBooks.getJSONObject(i).getInt("chapters") + "章");

                                LinearLayout tagsView = bookRow.findViewById(R.id.tags);
                                String tagStr = newBooks.getJSONObject(i).getString("tags");
                                String[] tags = tagStr.split(" ");
                                for(String tag : tags){
                                    TextView tagView = new TextView(FavoriteActivity.this);
                                    tagView.setText(tag);
                                    tagsView.addView(tagView);
                                }

                                if(ismanaging) {
                                    CheckBox checkBox = bookRow.findViewById(R.id.checkBox);
                                    checkBox.setVisibility(View.VISIBLE);
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

                        if(ismanaging){
                            toManage();
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
                    }
                });

                //获取封面
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
