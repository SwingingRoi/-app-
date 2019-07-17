package com.example.myapplication.Activity.Work;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.InternetUtils.HttpUtils;
import com.example.myapplication.AudioUtils.MilliToHMS;
import com.example.myapplication.MyComponent.MyToast;
import com.example.myapplication.R;

import com.example.myapplication.AudioUtils.AudioUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EditChapterActivity extends AppCompatActivity {

    private LinearLayout loadingView;
    private LinearLayout normal;
    private SeekBar seekBar;//进度条
    private EditText content;
    private boolean textChanged = false;
    private boolean speechChanged = false;//同步音频与文本

    private int chapterID;

    private String oldSpeechPath;
    private String newSpeechPath;
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
        setContentView(R.layout.activity_edit_chapter);
        MP3_LOCATION = this.getCacheDir().getAbsolutePath()+"/temp.mp3";
        BGM_LOCATION = this.getCacheDir().getAbsolutePath()+"/bgm.mp3";

        Intent intent = getIntent();
        chapterID = intent.getIntExtra("id",-1);

        normal = findViewById(R.id.normal);

        content = findViewById(R.id.content);
        content.addTextChangedListener(watcher);

        EditText content = findViewById(R.id.content);
        content.setMovementMethod(ScrollingMovementMethod.getInstance());

        loadingView = findViewById(R.id.Loading);
        loadingView.setVisibility(View.VISIBLE);
        normal.setVisibility(View.INVISIBLE);

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

                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView begin = findViewById(R.id.begin);
                            MilliToHMS milliToHMS = new MilliToHMS();
                            begin.setText(milliToHMS.milliToHMS(speech_player.getCurrentPosition()));
                        }
                    });
                }
            }
        });//实现拖动进度条，调整播放进度

        speech_player = new MediaPlayer();
        bgm_player = new MediaPlayer();

        speech_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                resetPlayer();

                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView playButton = findViewById(R.id.PlayButton);
                        playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));
                    }
                });
            }
        });

        refresh();
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textChanged = true;
            speechChanged = false;//文本修改了，音频尚未修改
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //重置播放状态
    private void resetPlayer(){
        if(speech_player != null){
            speech_player.reset();
        }

        if(bgm_player != null){
            bgm_player.reset();
        }

        firtstPlay = true;
    }

    public void onBackPressed(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed(){
        //保存草稿
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("保存");
        builder.setMessage("是否保存修改?");

        if(speech_player != null && speech_player.isPlaying()) {
            speech_player.pause();
        }
        if(bgm_player != null && bgm_player.isPlaying()) {
            bgm_player.pause();
        }

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    //保存对章节的修改

                    EditText title = findViewById(R.id.title);
                    if(title.getText().toString().length() > 20){
                        new MyToast(EditChapterActivity.this,getResources().getString(R.string.titlelong));
                        return;
                    }

                    new Thread(updateChapter).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                EditChapterActivity.super.onBackPressed();
            }
        });

        builder.show();
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

    public void storeChapter(View view){
        EditText title = findViewById(R.id.title);
        if(title.getText().toString().length() > 20){
            new MyToast(EditChapterActivity.this,getResources().getString(R.string.titlelong));
            return;
        }

        if(speech_player != null && speech_player.isPlaying()) {
            speech_player.pause();
        }
        if(bgm_player != null && bgm_player.isPlaying()) {
            bgm_player.pause();
        }

        new Thread(updateChapter).start();
    }

    public void textToSpeech(View view){
        new Thread(textToSpeech).start();
    }

    public void playSpeech(View view){
        if(firtstPlay)  {
            new Thread(prepareSpeech).start();
        }

        else {
            new Thread(controlSpeech).start();
        }
    }


    Runnable controlSpeech = new Runnable() {
        @Override
        public void run() {
            if(speech_player.isPlaying()) {
                speech_player.pause();
                bgm_player.pause();
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView playButton = findViewById(R.id.PlayButton);
                        playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));
                    }
                });
            }
            else {
                speech_player.start();
                bgm_player.start();
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView playButton = findViewById(R.id.PlayButton);
                        playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pause));
                    }
                });
            }
        }
    };

    //试听音频
    Runnable prepareSpeech = new Runnable() {
        @Override
        public void run() {
            try {
                //没有音频或音频尚未转换成功
                if(speechFile == null){
                    new MyToast(EditChapterActivity.this,"语音文件不存在!");
                    return;
                }
                //首次播放设置数据源
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


                        speech_player.start();
                        bgm_player.start();

                        bgm_player.setVolume(0.4f,0.4f);//设置背景音乐音量
                        bgm_player.setLooping(true);//背景音乐循环播放

                        //进度条更新
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!Thread.currentThread().isInterrupted()){
                                    try {
                                        if(speech_player == null) break;
                                        seekBar.setProgress(speech_player.getCurrentPosition());
                                        EditChapterActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                TextView begin = findViewById(R.id.begin);
                                                MilliToHMS milliToHMS = new MilliToHMS();
                                                begin.setText(milliToHMS.milliToHMS(speech_player.getCurrentPosition()));
                                            }
                                        });
                                        Thread.sleep(200);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();

                        EditChapterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firtstPlay = false;
                                ImageView playButton = findViewById(R.id.PlayButton);
                                playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pause));
                            }
                        });
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable textToSpeech = new Runnable() {
        @Override
        public void run() {
            try{

                //重置播放状态
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speechFile = null;
                        resetPlayer();

                        LinearLayout translating = findViewById(R.id.translating);
                        translating.setVisibility(View.VISIBLE);
                    }
                });

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/textToSpeech";

                EditText content = findViewById(R.id.content);
                JSONObject params = new JSONObject();
                params.put("text",content.getText());

                byte[] param = params.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(param, "POST", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }

                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            firtstPlay = true;

                            speechFile = new File(MP3_LOCATION);//speechFile保存后端语音
                            if (!speechFile.exists()) speechFile.createNewFile();
                            OutputStream outputStream = new FileOutputStream(speechFile);

                            resultStream.writeTo(outputStream);
                            speechChanged = true;//音频修改了

                            AudioUtils audioUtils = new AudioUtils();
                            MilliToHMS milliToHMS = new MilliToHMS();
                            TextView end = findViewById(R.id.end);
                            end.setText(milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));

                            seekBar.setProgress(0);
                            TextView begin = findViewById(R.id.begin);
                            begin.setText(getResources().getString(R.string.initial));

                            new Thread(matchBGM).start();
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

    Runnable matchBGM = new Runnable() {
        @Override
        public void run() {
            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/matchBGM";

                EditText content = findViewById(R.id.content);
                JSONObject params = new JSONObject();
                params.put("text",content.getText());

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(params.toString().getBytes(), "POST", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }

                final String result = new String(resultStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject path = new JSONObject(result);


                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bgmPath = path.getString("bgmPath");
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

                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bgm = new File(BGM_LOCATION);
                            if (!bgm.exists()) bgm.createNewFile();
                            OutputStream outputStream = new FileOutputStream(bgm);
                            resultStream.writeTo(outputStream);
                            outputStream.close();

                            new MyToast(EditChapterActivity.this, getResources().getString(R.string.translateSuccess));
                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);
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
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                final String result = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject chapter = new JSONObject(result);

                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EditText title = findViewById(R.id.title);
                            title.setText(chapter.getString("title"));

                            EditText content = findViewById(R.id.content);
                            content.setText(chapter.getString("content"));

                            oldSpeechPath = chapter.getString("speechPath");

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

    Runnable updateChapter = new Runnable() {
        @Override
        public void run() {
            try{

                if(textChanged && !speechChanged){//文本修改了，但语音尚未修改，提醒用户按下转换按钮
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditChapterActivity.this,getResources().getString(R.string.askpush));
                        }
                    });
                    return;
                }

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/modifychapter";

                JSONObject object = new JSONObject();
                object.put("id",chapterID);

                EditText title = findViewById(R.id.title);
                object.put("title",title.getText());

                EditText content = findViewById(R.id.content);
                object.put("content",content.getText());

                newSpeechPath = System.currentTimeMillis() + ".mp3";
                object.put("speechPath",newSpeechPath);

                object.put("bgmPath",bgmPath);

                AudioUtils audioUtils = new AudioUtils();
                MilliToHMS milliToHMS = new MilliToHMS();
                object.put("length",milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));

                byte[] param = object.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "GET", "application/json");

                new Thread(updateSpeech).start();

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
                String url = getServer.getIPADDRESS()+"/audiobook/getSpeech?path=" + oldSpeechPath;

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                EditChapterActivity.this.runOnUiThread(new Runnable() {
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

                            loadingView.setVisibility(View.INVISIBLE);
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

    Runnable updateSpeech = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/updateSpeech?oldpath=" + oldSpeechPath + "&newpath=" + newSpeechPath;

                FileInputStream inputStream = new FileInputStream(speechFile);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int n;
                byte[] b = new byte[1024];
                while ((n = inputStream.read(b)) != -1){
                    byteArrayOutputStream.write(b,0,n);
                }
                inputStream.close();
                byteArrayOutputStream.close();
                byte[] param = byteArrayOutputStream.toByteArray();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST",
                        "application/json");

                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(EditChapterActivity.this,"修改成功!");
                        speechFile.delete();

                        if(speech_player != null) {
                            speech_player.release();
                            speech_player = null;
                        }
                        if(bgm_player != null) {
                            bgm_player.release();
                            bgm_player = null;
                        }

                        EditChapterActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
}
