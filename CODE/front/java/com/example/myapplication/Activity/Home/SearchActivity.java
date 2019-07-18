package com.example.myapplication.Activity.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchBox;
    private LinearLayout bookTable;private LinearLayout normal;
    private LinearLayout loadView;
    private ScrollView scrollView;
    private View pullDown;//请求文字提示
    private int from=0;
    private int PAGESIZE=10;
    private JSONArray searchresults;
    private boolean isRequesting = true;//当前是否在向后端请求书本信息

    final private int SEARCHBTN = 1;
    final private int SCROLL = 2;
    private int SEARCHREQ = SEARCHBTN;
    private String searchWhat;
    private List<Drawable> tag_border_styles;//标签边框样式



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all_book);

        tag_border_styles = new ArrayList<>();
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_red));
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_brown));
        tag_border_styles.add(getResources().getDrawable(R.drawable.book_tag_border_blue));

        searchBox = findViewById(R.id.SearchBox);
        bookTable = findViewById(R.id.BookTable);
        normal = findViewById(R.id.normal);
        loadView = findViewById(R.id.Loading);
        pullDown = LayoutInflater.from(this).inflate(R.layout.pull_down, null);

        scrollView = findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if (!isRequesting && scrollView.getChildAt(0).getMeasuredHeight() <= scrollView.getScrollY() + scrollView.getHeight()) {
                        isRequesting = true;
                        SEARCHREQ = SCROLL;
                        new Thread(search).start();//向后端请求更多书本
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

    private void jumpToBook(int bookid){
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra("id",bookid);
        startActivity(intent);
    }

    public void doSearch(View view){
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


        searchWhat = searchBox.getText().toString();
        searchresults = new JSONArray();
        bookTable.removeAllViews();
        from = 0;//清空上一次的搜索结果
        SEARCHREQ = SEARCHBTN;
        isRequesting = true;
        new Thread(search).start();
    }

    Runnable search = new Runnable() {
        @Override
        public void run() {

            SearchActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView text = pullDown.findViewById(R.id.content);
                    text.setText(getResources().getString(R.string.isReq));
                }
            });


            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/searchbook?search=" +
                        URLEncoder.encode(searchWhat,"UTF-8") +//UTF-8 encode防止searchWhat为中文
                        "&from=" + from + "&size=" + PAGESIZE;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if(outputStream == null){
                    SearchActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(SearchActivity.this, getResources().getString(R.string.HttpTimeOut));
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

                SearchActivity.this.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try {

                            if(SEARCHREQ == SCROLL) {
                                bookTable.removeView(pullDown);
                            }

                            if (searchresults.length() == 0) {//搜索结果为空
                                SearchActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadView.setVisibility(View.INVISIBLE);
                                        View noresult = LayoutInflater.from(SearchActivity.this).inflate(R.layout.search_no_result, null);
                                        bookTable.removeAllViews();
                                        bookTable.addView(noresult);
                                    }
                                });
                                return;
                            }

                            for (int i = 0; i < resultArray.length(); i++) {
                                View bookRow = LayoutInflater.from(SearchActivity.this).inflate(R.layout.book_row_style, null);

                                TextView title = bookRow.findViewById(R.id.BookName);
                                title.setText(resultArray.getJSONObject(i).getString("name"));

                                TextView viewNumber = bookRow.findViewById(R.id.viewnumber);
                                viewNumber.setText(String.valueOf(resultArray.getJSONObject(i).getInt("views")));

                                TextView chapterNumber = bookRow.findViewById(R.id.chapternumber);
                                chapterNumber.setText(resultArray.getJSONObject(i).getInt("chapters") + "章");

                                LinearLayout tagsView = bookRow.findViewById(R.id.tags);
                                String tagStr = resultArray.getJSONObject(i).getString("tags");
                                String[] tags = tagStr.split(" ");

                                for(int j=0;j<tags.length;j++){
                                    String tag = tags[j];
                                    View tagView = LayoutInflater.from(SearchActivity.this).inflate(R.layout.book_tag,null);
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

                                final int id = resultArray.getJSONObject(i).getInt("id");
                                bookRow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        jumpToBook(id);
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

                SearchActivity.this.runOnUiThread(new Runnable() {
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
                final Bitmap surface = getPicture.getSurface(searchresults.getJSONObject(index).getInt("id"));

                SearchActivity.this.runOnUiThread(new Runnable() {
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

    private class DeleteSurface extends Thread{
        private String surfaceName;

        public DeleteSurface(String surfaceName){
            this.surfaceName = surfaceName;
        }

        @Override
        public void run() {
            try {
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS() + "/audiobook/deletesurface?filename=" + this.surfaceName;

                try{
                    HttpUtils httpUtils = new HttpUtils(url);
                    httpUtils.doHttp(null, "GET",
                            "application/json");

                }catch (Exception e){
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
