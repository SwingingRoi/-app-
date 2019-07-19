package com.example.myapplication.Activity.Work;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.AudioUtils.AudioUtils;
import com.example.myapplication.AudioUtils.MilliToHMS;
import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.InternetUtils.HttpUtils;
import com.example.myapplication.MyComponent.MyToast;
import com.example.myapplication.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NewChapterForSTTActivity extends AppCompatActivity {

    private LinearLayout normal;
    private SeekBar seekBar;//进度条
    private boolean speechChanged = false;//同步音频与文本

    private int bookid;
    private String chapterTitle;
    private String speechPath;
    private String bgmPath;

    private File speechFile = null;
    private File bgm = null;
    private String MP3_LOCATION;
    private String BGM_LOCATION;

    private MediaPlayer speech_player;//音频播放
    private MediaPlayer bgm_player;//bgm播放

    private boolean firtstPlay = true;//是否首次播放当前音频
    final private int GET_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chapter_for_stt);

        MP3_LOCATION = this.getCacheDir().getAbsolutePath()+"/temp.mp3";
        BGM_LOCATION = this.getCacheDir().getAbsolutePath()+"/bgm.mp3";

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        normal = findViewById(R.id.normal);

        TextView content = findViewById(R.id.content);
        content.setMovementMethod(ScrollingMovementMethod.getInstance());

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

                    NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
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

        Intent intent = getIntent();
        bookid = intent.getIntExtra("bookid",-1);

        speech_player = new MediaPlayer();
        bgm_player = new MediaPlayer();

        speech_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                resetPlayer();

                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView playButton = findViewById(R.id.PlayButton);
                        playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));
                    }
                });
            }
        });
    }

    private void resetPlayer(){
        if(speech_player != null){
            speech_player.reset();
        }

        if(bgm_player != null){
            bgm_player.reset();
        }

        firtstPlay = true;
    }

    public void playSpeech(View view){
        if(firtstPlay)  {
            new Thread(prepareSpeech).start();
        }

        else {
            new Thread(controlSpeech).start();
        }
    }

    public void onBackPressed(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed(){
        if(speech_player != null && speech_player.isPlaying()) {
            speech_player.pause();
        }
        if(bgm_player != null && bgm_player.isPlaying()) {
            bgm_player.pause();
        }

        //保存草稿
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("退出");
        builder.setMessage("确认退出吗?");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    dialog.dismiss();

                    if(speech_player != null) {
                        speech_player.reset();
                        speech_player.release();
                        speech_player = null;
                    }
                    if(bgm_player != null) {
                        bgm_player.reset();
                        bgm_player.release();
                        bgm_player = null;
                    }

                    if(speechFile != null && speechFile.exists()) speechFile.delete();
                    if(bgm != null && bgm.exists()) bgm.delete();

                    NewChapterForSTTActivity.super.onBackPressed();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public void SpeechToText(View view){
        if(speechFile == null){
            new MyToast(NewChapterForSTTActivity.this,getResources().getString(R.string.askchoosefile));
            return;
        }

        new Thread(SpeechToText).start();
    }

    //保存章节
    public void storeChapter(View view){
        if(speechChanged){
            new MyToast(NewChapterForSTTActivity.this,getResources().getString(R.string.askpush));
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View v = LayoutInflater.from(this).inflate(R.layout.dialog_layout,null);
        TextView titleView = v.findViewById(R.id.Title);
        titleView.setText("标题");

        TextView text = findViewById(R.id.content);
        final String content = text.getText().toString();

        builder.setView(v);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText title = v.findViewById(R.id.Edit);
                chapterTitle = title.getText().toString();

                if(chapterTitle.length() > 20){//标题不能超过20个字
                    new MyToast(NewChapterForSTTActivity.this,getResources().getString(R.string.titlelong));
                    return;
                }

                speechPath = System.currentTimeMillis() + ".mp3";

                StoreChapter storeChapter = new StoreChapter(chapterTitle,bookid,content,speechPath,bgmPath);
                new Thread(storeChapter).start();
                new Thread(storeSpeech).start();
            }
        });

        builder.setNegativeButton("取消", null);

        builder.show();
    }

    //选择文件
    public void chooseFile(View view){
        speechChanged = true;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,GET_FILE);
    }

    @Override
    //选择文件的回调函数
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        try {
            if (resultCode == Activity.RESULT_CANCELED) return;

            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == GET_FILE) {
                    if(data != null) {
                        Uri uri = data.getData();
                        if(uri != null) {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            OutputStream outputStream = new FileOutputStream(MP3_LOCATION);
                            byte[] buffer = new byte[1024];
                            int len;

                            if(inputStream != null) {
                                while ((len = inputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer,0,len);
                                }
                            }

                            outputStream.close();
                            inputStream.close();

                            speechFile = new File(MP3_LOCATION);

                            //更新MP3文件时长
                            NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AudioUtils audioUtils = new AudioUtils();
                                    MilliToHMS milliToHMS = new MilliToHMS();
                                    TextView end = findViewById(R.id.end);
                                    end.setText(milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));
                                }
                            });

                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Runnable controlSpeech = new Runnable() {
        @Override
        public void run() {
            if(speech_player.isPlaying()) {
                speech_player.pause();
                bgm_player.pause();
                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
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
                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
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
                    new MyToast(NewChapterForSTTActivity.this,"语音文件不存在!");
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

                        bgm_player.setVolume(0.2f,0.2f);//设置背景音乐音量
                        bgm_player.setLooping(true);//背景音乐循环播放

                        //进度条更新
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!Thread.currentThread().isInterrupted()){
                                    try {
                                        if(speech_player == null) break;
                                        seekBar.setProgress(speech_player.getCurrentPosition());
                                        NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
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

                        NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
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

    Runnable SpeechToText = new Runnable() {
        @Override
        public void run() {
            try{

                //重置播放状态
                resetPlayer();
                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(0);
                        TextView begin = findViewById(R.id.begin);
                        begin.setText(getResources().getString(R.string.initial));
                    }
                });

                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout translating = findViewById(R.id.translating);
                        translating.setVisibility(View.VISIBLE);
                    }
                });

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/SpeechToText";

                InputStream inputStream = new FileInputStream(MP3_LOCATION);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer,0,len);
                }
                inputStream.close();

                byte[] param = byteArrayOutputStream.toByteArray();

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(param, "POST", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterForSTTActivity.this, getResources().getString(R.string.HttpTimeOut));

                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }

                firtstPlay = true;

                final String result = new String(resultStream.toByteArray(),
                        StandardCharsets.UTF_8);


                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TextView content = findViewById(R.id.content);
                            content.setText(result);

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

                TextView contentView = findViewById(R.id.content);
                JSONObject params = new JSONObject();
                params.put("text",contentView.getText().toString());

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(params.toString().getBytes(), "POST", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterForSTTActivity.this, getResources().getString(R.string.HttpTimeOut));

                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }

                final String result = new String(resultStream.toByteArray(),
                        StandardCharsets.UTF_8);

                final JSONObject path = new JSONObject(result);



                bgmPath = path.getString("bgmPath");
                new Thread(getBgm).start();

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

                bgm = new File(BGM_LOCATION);
                if (!bgm.exists()) bgm.createNewFile();
                OutputStream outputStream = new FileOutputStream(bgm);
                resultStream.writeTo(outputStream);
                outputStream.close();


                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            new MyToast(NewChapterForSTTActivity.this, getResources().getString(R.string.translateSuccess));
                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);

                            ImageView playButton = findViewById(R.id.PlayButton);
                            playButton.setVisibility(View.VISIBLE);

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

    private class StoreChapter extends Thread
    {
        private String title;
        private int bookid;
        private String content;
        private String speechPath;
        private String bgmPath;

        public StoreChapter(String title,int bookid,String content,String speechPath,String bgmPath){
            this.title = title;
            this.bookid = bookid;
            this.content = content;
            this.speechPath = speechPath;
            this.bgmPath = bgmPath;
        }

        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/storeChapter";

                JSONObject info = new JSONObject();
                info.put("title",title);
                info.put("bookid",bookid);
                info.put("content",content);
                info.put("speechPath",speechPath);
                info.put("bgmPath",bgmPath);

                AudioUtils audioUtils = new AudioUtils();
                MilliToHMS milliToHMS = new MilliToHMS();
                info.put("length",milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));

                byte[]param = info.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST", "application/json");

                if(speech_player != null && speech_player.isPlaying()) {
                    speech_player.pause();
                }
                if(bgm_player != null && bgm_player.isPlaying()) {
                    bgm_player.pause();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    Runnable storeSpeech = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/storeSpeech?path=" + speechPath;

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

                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(NewChapterForSTTActivity.this,"创建成功!");
                        if(speechFile.exists()) speechFile.delete();

                        if(speech_player != null) {
                            speech_player.release();
                            speech_player = null;
                        }
                        if(bgm_player != null) {
                            bgm_player.release();
                            bgm_player = null;
                        }

                        if(speechFile != null && speechFile.exists()) speechFile.delete();
                        if(bgm != null && bgm.exists()) bgm.delete();
                        NewChapterForSTTActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
}
