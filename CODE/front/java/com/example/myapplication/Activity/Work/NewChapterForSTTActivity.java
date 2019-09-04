package com.example.myapplication.Activity.Work;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
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

    private SeekBar seekBar;//进度条
    private boolean speechChanged = false;//同步音频与文本

    private int bookid;
    private String chapterTitle;
    private String speechPath;
    private String text;//转换后的文本
    private String bgmPath;
    private Uri fileUri;

    private File speechFile = null;
    private File bgm = null;
    private String MP3_LOCATION;
    private String BGM_LOCATION;
    private boolean getSpeechDone = false;
    private boolean matchBgmDone = false;//音频时长

    final private int WRITE_EXTERNAL_STORAGE = 1;

    private MediaPlayer speech_player;//音频播放
    private MediaPlayer bgm_player = null;//bgm播放

    private boolean firstPlay = true;//是否首次播放当前音频
    final private int GET_FILE = 1;
    private boolean isInNight = false;//是否处于夜间模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_new_chapter_for_stt_night);
        }else {
            setContentView(R.layout.activity_new_chapter_for_stt);
        }

        MP3_LOCATION = this.getCacheDir().getAbsolutePath()+"/temp.mp3";
        BGM_LOCATION = this.getCacheDir().getAbsolutePath()+"/bgm.mp3";


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
            }
        });
    }

    private void setAnimation(){
        ImageView rotation = findViewById(R.id.rotation);
        Animation rotate = AnimationUtils.loadAnimation(this,R.anim.image_rotate);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        rotate.setInterpolator(linearInterpolator);
        rotation.setAnimation(rotate);
        rotation.startAnimation(rotate);
    }

    private void clearAnimation(){
        ImageView rotation = findViewById(R.id.rotation);
        rotation.clearAnimation();
    }

    private void resetPlayer(){
        if(speech_player != null){
            speech_player.reset();
        }

        if(bgm_player != null){
            bgm_player.reset();
        }

        NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView playButton = findViewById(R.id.PlayButton);
                playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));
                clearAnimation();
            }
        });
        firstPlay = true;
    }

    private void releasePlayer(){
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
    }

    public void playSpeech(View view){

        if(speechFile == null || new AudioUtils().getLength(MP3_LOCATION) == 0){
            new MyToast(NewChapterForSTTActivity.this,"语音文件不存在!");
            return;
        }

        if(firstPlay)  {
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
        //保存草稿
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("退出");
        builder.setMessage("确认退出吗?");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    dialog.dismiss();

                    releasePlayer();

                    if(speechFile != null && speechFile.exists()) speechFile.delete();
                    if(bgm != null && bgm.exists()) bgm.delete();

                    new Thread(deleteSpeech).start();

                    NewChapterForSTTActivity.super.finish();
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
        System.out.println("asfads");
        if(speechFile == null){
            new MyToast(NewChapterForSTTActivity.this,getResources().getString(R.string.askchoosefile));
            return;
        }

        new Thread(SpeechToText).start();
    }

    //保存章节
    public void storeChapter(View view){
        if(speechChanged){
            new MyToast(NewChapterForSTTActivity.this,"请转换修改后的音频");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View v = LayoutInflater.from(this).inflate(R.layout.dialog_layout,null);
        TextView titleView = v.findViewById(R.id.Title);
        titleView.setText("标题");

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

                StoreChapter storeChapter = new StoreChapter(chapterTitle,bookid,"",speechPath,bgmPath);
                storeChapter.start();
                releasePlayer();

                if(speechFile != null && speechFile.exists()) speechFile.delete();
                if(bgm != null && bgm.exists()) bgm.delete();
                NewChapterForSTTActivity.super.finish();
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
                        fileUri = data.getData();
                        if(fileUri != null) {
                            //查看是否有访问external_storage的权限，没有则申请
                            int permission = ActivityCompat.checkSelfPermission(NewChapterForSTTActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if(permission != PackageManager.PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(NewChapterForSTTActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        WRITE_EXTERNAL_STORAGE);
                            }
                            else {
                                UpdateUIAfterChooseFile updateUIAfterChooseFile = new UpdateUIAfterChooseFile(fileUri);
                                updateUIAfterChooseFile.start();
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String []permissions, int[] grantResults){
        if (requestCode == WRITE_EXTERNAL_STORAGE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                UpdateUIAfterChooseFile updateUIAfterChooseFile = new UpdateUIAfterChooseFile(fileUri);
                updateUIAfterChooseFile.start();
            }
        }
    }

    private class UpdateUIAfterChooseFile extends Thread{
        Uri uri;

        public UpdateUIAfterChooseFile(Uri uri){
            this.uri = uri;
        }

        @Override
        public void run(){
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                OutputStream outputStream = new FileOutputStream(MP3_LOCATION);
                byte[] buffer = new byte[1024];
                int len;

                if (inputStream != null) {
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
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
            }catch (Exception e){
                e.printStackTrace();
            }
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
                        clearAnimation();
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
                        setAnimation();
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
                                firstPlay = false;
                                ImageView playButton = findViewById(R.id.PlayButton);
                                playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pause));

                                setAnimation();
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
                speechChanged = false;

                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(0);

                        TextView begin = findViewById(R.id.begin);
                        begin.setText(getResources().getString(R.string.initial));

                        TextView end = findViewById(R.id.end);
                        end.setText(getResources().getString(R.string.initial));

                        LinearLayout translating = findViewById(R.id.translating);
                        translating.setVisibility(View.VISIBLE);
                    }
                });

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/speechToText";

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

                final String result = new String(resultStream.toByteArray(),
                        StandardCharsets.UTF_8);

                if(result.equals("error")){
                    NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterForSTTActivity.this,"请选择合适的录音文件!");
                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);

                            ImageView playButton = findViewById(R.id.PlayButton);
                            playButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                JSONObject resultObj = new JSONObject(result);

                speechPath = resultObj.getString("speechPath");
                text = resultObj.getString("text");

                System.out.println(speechPath);
                System.out.println(text);
                new Thread(getSpeech).start();
                new Thread(matchBGM).start();

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

                speechFile = new File(MP3_LOCATION);//speechFile保存后端语音
                if (!speechFile.exists()) speechFile.createNewFile();
                OutputStream outputStream = new FileOutputStream(speechFile);
                resultStream.writeTo(outputStream);
                speechChanged = false;
                outputStream.close();


                getSpeechDone = true;

                if(matchBgmDone){
                    getSpeechDone = false;
                    matchBgmDone = false;

                    NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterForSTTActivity.this, getResources().getString(R.string.translateSuccess));
                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);

                            ImageView playButton = findViewById(R.id.PlayButton);
                            playButton.setVisibility(View.VISIBLE);

                            AudioUtils audioUtils = new AudioUtils();
                            MilliToHMS milliToHMS = new MilliToHMS();
                            TextView end = findViewById(R.id.end);
                            end.setText(milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable deleteSpeech = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/deleteSpeech?path=" + speechPath;

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

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
                String url = getServer.getIPADDRESS()+"/audiobook/matchBGMByText";

                JSONObject params = new JSONObject();
                params.put("text",text);

                byte[] param = params.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream resultStream = httpUtils.doHttp(param, "POST",
                        "application/json");

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


                matchBgmDone = true;

                if(getSpeechDone){
                    matchBgmDone = false;
                    getSpeechDone = false;

                    NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterForSTTActivity.this, getResources().getString(R.string.translateSuccess));
                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);

                            ImageView playButton = findViewById(R.id.PlayButton);
                            playButton.setVisibility(View.VISIBLE);

                            AudioUtils audioUtils = new AudioUtils();
                            MilliToHMS milliToHMS = new MilliToHMS();
                            TextView end = findViewById(R.id.end);
                            end.setText(milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));
                        }
                    });
                }

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

                TextView endView = findViewById(R.id.end);
                info.put("length",endView.getText().toString());

                byte[]param = info.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST", "application/json");

                NewChapterForSTTActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(NewChapterForSTTActivity.this,"创建成功!");
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
