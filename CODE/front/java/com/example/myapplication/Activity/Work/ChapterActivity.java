package com.example.myapplication.Activity.Work;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.GetServer;
import com.example.myapplication.HttpUtils;
import com.example.myapplication.MyToast;
import com.example.myapplication.R;
import com.example.myapplication.MilliToHMS;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChapterActivity extends AppCompatActivity {

    private LinearLayout loadingView;
    private LinearLayout normal;
    private SeekBar seekBar;//进度条
    private TextView end;

    private int chapterID;
    private int bookid;
    private String account;
    private String speechPath;
    private String bgmPath;

    private File speechFile = null;
    private File bgm = null;
    private String MP3_LOCATION;
    private String BGM_LOCATION;

    private MediaPlayer speech_player;//音频播放
    private MediaPlayer bgm_player;//bgm播放

    private boolean firtstPlay = true;//是否首次播放当前音频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        MP3_LOCATION = this.getCacheDir().getAbsolutePath()+"/temp.mp3";
        BGM_LOCATION = this.getCacheDir().getAbsolutePath()+"/bgm.mp3";

        Intent intent = getIntent();
        chapterID = intent.getIntExtra("chapterId",-1);
        bookid = intent.getIntExtra("bookid",-1);



        TextView content = findViewById(R.id.content);
        content.setMovementMethod(ScrollingMovementMethod.getInstance());

        normal = findViewById(R.id.normal);

        loadingView = findViewById(R.id.Loading);
        loadingView.setVisibility(View.VISIBLE);
        normal.setVisibility(View.INVISIBLE);
        end = findViewById(R.id.end);

        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        account = sharedPreferences.getString("Account","");

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(speechFile != null){
                    speech_player.seekTo(seekBar.getProgress());
                    bgm_player.seekTo(seekBar.getProgress());

                    TextView begin = findViewById(R.id.begin);
                    MilliToHMS milliToHMS = new MilliToHMS();
                    begin.setText(milliToHMS.milliToHMS(seekBar.getProgress()));
                }
            }
        });//实现拖动进度条，调整播放进度

        speech_player = new MediaPlayer();
        speech_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                resetPlayer();
            }
        });

        bgm_player = new MediaPlayer();

        new Thread(addRecord).start();//添加浏览记录
        refresh();
    }

    public void onBackPressed(View view){
        super.onBackPressed();
    }

    @Override
    public void onBackPressed(){
        if(speech_player != null) {
            speech_player.pause();
            speech_player.release();
        }
        if(bgm_player != null) {
            bgm_player.pause();
            bgm_player.release();
        }
        super.onBackPressed();
    }

    //重置播放状态
    private void resetPlayer(){
        speech_player.reset();
        bgm_player.reset();

        ImageView playButton = findViewById(R.id.PlayButton);
        playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));

        firtstPlay = true;
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

        new Thread(getChapter).start();
    }

    //刷新界面
    public void refresh(View view){
        refresh();
    }

    //试听音频
    public void playSpeech(View view){
        try {
            //没有音频或音频尚未转换成功
            if(speechFile == null){
                new MyToast(this,"语音文件不存在!");
                return;
            }

            //播放
            if(!speech_player.isPlaying()) {

                //首次播放设置数据源
                if(firtstPlay) {
                    seekBar.setProgress(0);

                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();//数据源类型

                    speech_player.setDataSource(MP3_LOCATION);
                    speech_player.setAudioAttributes(audioAttributes);

                    bgm_player.setDataSource(BGM_LOCATION);
                    bgm_player.setAudioAttributes(audioAttributes);


                    bgm_player.prepare();
                    speech_player.prepareAsync();//异步准备音源
                            speech_player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    seekBar.setMax(speech_player.getDuration());

                                    //让进度条与播放进度同步
                                    Timer timer = new Timer();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            if(!speech_player.isPlaying()) return;
                                            seekBar.setProgress(speech_player.getCurrentPosition());

                                            normal.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    TextView begin = findViewById(R.id.begin);
                                                    MilliToHMS milliToHMS = new MilliToHMS();
                                                    begin.setText(milliToHMS.milliToHMS(speech_player.getCurrentPosition()));
                                                }
                                            });
                                        }
                                    };
                                    timer.schedule(task,0,10);

                                    speech_player.start();
                                    bgm_player.start();
                                    bgm_player.setVolume(0.1f,0.1f);//设置背景音乐音量
                                    bgm_player.setLooping(true);//背景音乐循环播放


                                    firtstPlay = false;
                                    ImageView playButton = findViewById(R.id.PlayButton);
                                    playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pause));
                                }
                            });

                }

                //非首次播放从暂停状态恢复
                else {
                    ImageView playButton = findViewById(R.id.PlayButton);
                    playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pause));
                    speech_player.start();
                    bgm_player.start();
                }
            }

            //暂停
            else {
                speech_player.pause();
                bgm_player.pause();
                ImageView playButton = findViewById(R.id.PlayButton);
                playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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

    Runnable getChapter = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getchapterbyid?id="
                        + chapterID;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(ChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject chapter = new JSONObject(result);

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TextView title = findViewById(R.id.title);
                            title.setText(chapter.getString("title"));

                            TextView content = findViewById(R.id.content);
                            content.setText(chapter.getString("content"));

                            speechPath = chapter.getString("speechPath");
                            bgmPath = chapter.getString("bgmPath");

                            TextView end = findViewById(R.id.end);
                            end.setText(chapter.getString("length"));

                            new Thread(getSpeech).start();
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

    Runnable getSpeech = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getSpeech?path=" + speechPath;

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(ChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            speechFile = new File(MP3_LOCATION);//speechFile保存后端语音
                            if (!speechFile.exists()) speechFile.createNewFile();
                            OutputStream outputStream = new FileOutputStream(speechFile);

                            resultStream.writeTo(outputStream);

                            seekBar.setProgress(0);

                            TextView begin = findViewById(R.id.begin);
                            begin.setText(getResources().getString(R.string.initial));

                            new Thread(getBgm).start();
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

    Runnable getBgm = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getBGM?filename=" + URLEncoder.encode(bgmPath,"UTF-8");

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bgm = new File(BGM_LOCATION);//speechFile保存后端语音
                            if (!bgm.exists()) bgm.createNewFile();
                            OutputStream outputStream = new FileOutputStream(bgm);

                            resultStream.writeTo(outputStream);
                            loadingView.setVisibility(View.INVISIBLE);
                            normal.setVisibility(View.VISIBLE);
                            outputStream.close();
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
}
