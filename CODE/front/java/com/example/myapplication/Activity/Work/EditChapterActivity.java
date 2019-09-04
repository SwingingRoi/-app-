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
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
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
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EditChapterActivity extends AppCompatActivity {

    private LinearLayout loadingView;
    private LinearLayout normal;
    private SeekBar seekBar;//进度条
    private boolean textChanged = false;
    private boolean speechChanged = false;//同步音频与文本
    private boolean firstIn = true;//是否首次进入该界面
    private boolean hasPlayerReset = false;

    private int chapterID;

    private String oldSpeechPath;
    private String newSpeechPath;
    private String bgmPath;
    final private int GET_FILE = 1;
    private boolean getSpeechDone = false;
    private boolean getBgmDone = false;
    private boolean isInNight = false;//是否处于夜间模式

    private File speechFile = null;
    private File bgm = null;
    private String MP3_LOCATION;
    private String BGM_LOCATION;
    private Uri fileUri;
    final private int WRITE_EXTERNAL_STORAGE = 1;

    final private int TTS = 1;//text to speech
    final private int STT = 2;//speech to text
    private int chapterType = 0;


    private MediaPlayer speech_player;//音频播放
    private MediaPlayer bgm_player;//bgm播放

    private boolean firstPlay = true;//是否首次播放当前音频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_edit_chapter_night);
        }else {
            setContentView(R.layout.activity_edit_chapter);
        }

        MP3_LOCATION = this.getCacheDir().getAbsolutePath()+"/temp.mp3";
        BGM_LOCATION = this.getCacheDir().getAbsolutePath()+"/bgm.mp3";

        Intent intent = getIntent();
        chapterID = intent.getIntExtra("id",-1);

        normal = findViewById(R.id.normal);

        EditText content = findViewById(R.id.content);
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        content.addTextChangedListener(watcher);

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
        hasPlayerReset = true;
        if(speech_player != null){
            speech_player.reset();
        }

        if(bgm_player != null){
            bgm_player.reset();
        }

        firstPlay = true;

        if(EditChapterActivity.this.isFinishing()) return;
        EditChapterActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView begin = findViewById(R.id.begin);
                begin.setText(getResources().getString(R.string.initial));
                seekBar.setProgress(0);
                ImageView playButton = findViewById(R.id.PlayButton);
                playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));
            }
        });
    }

    //释放mediaPlayer资源
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

    public void onBackPressed(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed(){
        //保存草稿
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("保存");
        builder.setMessage("是否保存修改?");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    //保存对章节的修改

                    hasPlayerReset = true;
                    EditText title = findViewById(R.id.title);
                    if(title.getText().toString().length() > 20){
                        new MyToast(EditChapterActivity.this,getResources().getString(R.string.titlelong));
                        return;
                    }

                    releasePlayer();

                    DeleteSpeech deleteSpeech = new DeleteSpeech(oldSpeechPath);
                    deleteSpeech.start();

                    new Thread(updateChapter).start();
                    EditChapterActivity.this.finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                hasPlayerReset = true;
                releasePlayer();

                DeleteSpeech deleteSpeech = new DeleteSpeech(newSpeechPath);
                deleteSpeech.start();

                EditChapterActivity.this.finish();
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
                            int permission = ActivityCompat.checkSelfPermission(EditChapterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if(permission != PackageManager.PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(EditChapterActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        WRITE_EXTERNAL_STORAGE);
                            }
                            else {
                                ActionAfterChooseFile actionAfterChooseFile = new ActionAfterChooseFile(fileUri);
                                actionAfterChooseFile.start();
                            }

                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //存储修改后的章节
    public void storeChapter(View view){
        EditText title = findViewById(R.id.title);
        if(title.getText().toString().length() > 20){
            new MyToast(EditChapterActivity.this,getResources().getString(R.string.titlelong));
            return;
        }

        if(textChanged && !speechChanged){//文本修改了，但语音尚未修改，提醒用户按下转换按钮
            new MyToast(EditChapterActivity.this,getResources().getString(R.string.askpush));
            return;
        }

        releasePlayer();

        DeleteSpeech deleteSpeech = new DeleteSpeech(oldSpeechPath);
        deleteSpeech.start();
        new Thread(updateChapter).start();

        EditChapterActivity.this.finish();
    }

    //转换(TTS文本转语音,STT语音转文本
    public void translate(View view){
        resetPlayer();
        if(chapterType == STT) {
            new Thread(speechToText).start();
        }
        else {
            new Thread(textToSpeech).start();
        }
    }

    //播放音频
    public void playSpeech(View view){
        if(firstPlay)  {
            new Thread(prepareSpeech).start();
        }

        else {
            new Thread(controlSpeech).start();
        }
    }

    //更新转换后的UI
    private void updateUiAfterTrans(){
        new MyToast(EditChapterActivity.this, getResources().getString(R.string.translateSuccess));
        LinearLayout translating = findViewById(R.id.translating);
        translating.setVisibility(View.INVISIBLE);
        normal.setVisibility(View.VISIBLE);
        AudioUtils audioUtils = new AudioUtils();
        MilliToHMS milliToHMS = new MilliToHMS();
        TextView end = findViewById(R.id.end);
        end.setText(milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));
    }

    //转换超时更新Ui
    private void updateUiAfterTransTimeOut(){
        new MyToast(EditChapterActivity.this, getResources().getString(R.string.HttpTimeOut));
        LinearLayout translating = findViewById(R.id.translating);
        translating.setVisibility(View.INVISIBLE);
        normal.setVisibility(View.VISIBLE);
    }

    //控制语音播放
    Runnable controlSpeech = new Runnable() {
        @Override
        public void run() {
            if(speech_player.isPlaying()) {
                speech_player.pause();
                bgm_player.pause();
                if(EditChapterActivity.this.isFinishing()) return;
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
                if(EditChapterActivity.this.isFinishing()) return;
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

    //准备音频资源
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

                        bgm_player.setVolume(0.2f,0.2f);//设置背景音乐音量
                        bgm_player.setLooping(true);//背景音乐循环播放
                        hasPlayerReset = false;
                        //进度条更新
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!Thread.currentThread().isInterrupted()){
                                    try {
                                        if(EditChapterActivity.this.isFinishing()) break;
                                        if(speech_player == null || hasPlayerReset) break;
                                        seekBar.setProgress(speech_player.getCurrentPosition());
                                        EditChapterActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                TextView begin = findViewById(R.id.begin);
                                                MilliToHMS milliToHMS = new MilliToHMS();
                                                if(speech_player == null || hasPlayerReset) return;
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

                        if(EditChapterActivity.this.isFinishing()) return;
                        EditChapterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firstPlay = false;
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

    //语音转文本
    Runnable speechToText = new Runnable() {
        @Override
        public void run() {
            try {

                resetPlayer();
                speechChanged = false;

                if(EditChapterActivity.this.isFinishing()) return;
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        seekBar.setProgress(0);

                        TextView begin = findViewById(R.id.begin);
                        begin.setText(getResources().getString(R.string.initial));

                        TextView end = findViewById(R.id.end);
                        end.setText(getResources().getString(R.string.initial));

                        LinearLayout translating = findViewById(R.id.translating);
                        translating.setVisibility(View.VISIBLE);

                        normal.setVisibility(View.INVISIBLE);
                    }
                });

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS() + "/audiobook/speechToText";

                InputStream inputStream = new FileInputStream(MP3_LOCATION);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                inputStream.close();

                byte[] param = byteArrayOutputStream.toByteArray();

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(param, "POST", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    if(EditChapterActivity.this.isFinishing()) return;
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUiAfterTransTimeOut();
                        }
                    });
                    return;
                }

                final String result = new String(resultStream.toByteArray(),
                        StandardCharsets.UTF_8);

                if(result.equals("error")){
                    if(EditChapterActivity.this.isFinishing()) return;
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(EditChapterActivity.this,"请选择合适的录音文件!");
                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);
                            normal.setVisibility(View.VISIBLE);

                            ImageView playButton = findViewById(R.id.PlayButton);
                            playButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                JSONObject resultObj = new JSONObject(result);

                newSpeechPath = resultObj.getString("speechPath");
                final String text = resultObj.getString("text");

                GetSpeech getSpeech = new GetSpeech(newSpeechPath);
                getSpeech.start();

                MatchBGMByText matchBGMByText = new MatchBGMByText(text);
                matchBGMByText.start();

                if(EditChapterActivity.this.isFinishing()) return;
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView playButton = findViewById(R.id.PlayButton);
                        playButton.setVisibility(View.VISIBLE);

                        TextView textView = findViewById(R.id.sttText);
                        textView.setText(text);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //文本转语音
    Runnable textToSpeech = new Runnable() {
        @Override
        public void run() {
            try{
                resetPlayer();
                speechFile = null;
                textChanged = false;
                if(EditChapterActivity.this.isFinishing()) return;
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(0);

                        TextView begin = findViewById(R.id.begin);
                        begin.setText(getResources().getString(R.string.initial));

                        TextView end = findViewById(R.id.end);
                        end.setText(getResources().getString(R.string.initial));

                        LinearLayout translating = findViewById(R.id.translating);
                        translating.setVisibility(View.VISIBLE);

                        normal.setVisibility(View.INVISIBLE);
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
                    if(EditChapterActivity.this.isFinishing()) return;
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUiAfterTransTimeOut();
                        }
                    });
                    return;
                }

                newSpeechPath = new String(resultStream.toByteArray(),
                        StandardCharsets.UTF_8);

                GetSpeech getSpeech = new GetSpeech(newSpeechPath);
                getSpeech.start();

                MatchBGMByText matchBGMByText = new MatchBGMByText(content.getText().toString());
                matchBGMByText.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //将选择的语音文件存在MP3_LOCATION，同时更新UI上的音频时长
    private class ActionAfterChooseFile extends Thread{
        private Uri uri;

        public ActionAfterChooseFile(Uri uri){
            this.uri = uri;
        }

        @Override
        public void run(){
            try{

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

                if(EditChapterActivity.this.isFinishing()) return;
                EditChapterActivity.this.runOnUiThread(new Runnable() {
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

    //通过文本匹配BGM
    private class MatchBGMByText extends Thread{
        private String text;

        public MatchBGMByText(String text){
            this.text = text;
        }

        @Override
        public void run(){
            try{

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/matchBGMByText";

                JSONObject params = new JSONObject();
                params.put("text",text);

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(params.toString().getBytes(), "POST", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    if(EditChapterActivity.this.isFinishing()) return;
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUiAfterTransTimeOut();
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
    }

    //获取BGM
    Runnable getBgm = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getBGM?filename=" + URLEncoder.encode(bgmPath,"UTF-8");

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    if(EditChapterActivity.this.isFinishing()) return;
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(firstIn) {
                                new MyToast(EditChapterActivity.this, getResources().getString(R.string.HttpTimeOut));
                                findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                                findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                            }else {
                                updateUiAfterTransTimeOut();
                            }
                        }
                    });
                    return;
                }

                bgm = new File(BGM_LOCATION);
                if (!bgm.exists()) bgm.createNewFile();
                OutputStream outputStream = new FileOutputStream(bgm);
                resultStream.writeTo(outputStream);
                outputStream.close();

                getBgmDone = true;
                if(getSpeechDone){
                    getSpeechDone = false;
                    getBgmDone = false;
                    if(EditChapterActivity.this.isFinishing()) return;
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(firstIn){
                                    loadingView.setVisibility(View.INVISIBLE);
                                    normal.setVisibility(View.VISIBLE);
                                    firstIn = false;
                                }
                                else {
                                    updateUiAfterTrans();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //获取章节信息
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
                    if(EditChapterActivity.this.isFinishing()) return;
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

                oldSpeechPath = chapter.getString("speechPath");

                bgmPath = chapter.getString("bgmPath");

                GetSpeech getSpeech = new GetSpeech(oldSpeechPath);
                getSpeech.start();

                new Thread(getBgm).start();

                if(EditChapterActivity.this.isFinishing()) return;
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EditText title = findViewById(R.id.title);
                            title.setText(chapter.getString("title"));

                            String text = chapter.getString("content");

                            boolean type = chapter.getBoolean("type");
                            if(type){
                                chapterType = STT;
                                EditText content = findViewById(R.id.content);
                                content.setVisibility(View.GONE);

                                TextView textView = findViewById(R.id.sttText);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(text);

                                Button chooseFile = findViewById(R.id.choosefile);
                                chooseFile.setVisibility(View.VISIBLE);
                            }
                            else {
                                chapterType = TTS;
                                EditText content = findViewById(R.id.content);
                                content.setVisibility(View.VISIBLE);
                                content.setText(text);

                                TextView textView = findViewById(R.id.sttText);
                                textView.setVisibility(View.GONE);

                                Button chooseFile = findViewById(R.id.choosefile);
                                chooseFile.setVisibility(View.GONE);
                            }
                            TextView end = findViewById(R.id.end);
                            end.setText(chapter.getString("length"));
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

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/modifychapter";

                JSONObject object = new JSONObject();
                object.put("id",chapterID);

                EditText title = findViewById(R.id.title);
                object.put("title",title.getText());

                if(chapterType == STT){
                    TextView textView = findViewById(R.id.sttText);
                    object.put("content",textView.getText());
                }
                else {
                    EditText content = findViewById(R.id.content);
                    object.put("content", content.getText());
                }

                object.put("speechPath",newSpeechPath);

                object.put("bgmPath",bgmPath);

                AudioUtils audioUtils = new AudioUtils();
                MilliToHMS milliToHMS = new MilliToHMS();
                object.put("length",milliToHMS.milliToHMS(audioUtils.getLength(MP3_LOCATION)));

                byte[] param = object.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST", "application/json");

                speechFile.delete();
                EditChapterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(EditChapterActivity.this,"修改成功!");
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    private class GetSpeech extends Thread{

        private String path;

        public GetSpeech(String path){
            this.path = path;
        }

        @Override
        public void run() {
            try {
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS() + "/audiobook/getSpeech?path=" + path;

                HttpUtils httpUtils = new HttpUtils(url);
                final ByteArrayOutputStream resultStream = httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

                if (resultStream == null) {//请求超时
                    if(EditChapterActivity.this.isFinishing()) return;
                    EditChapterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(firstIn) {
                                new MyToast(EditChapterActivity.this, getResources().getString(R.string.HttpTimeOut));
                                findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                                findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                            }else {
                                updateUiAfterTransTimeOut();
                            }
                        }
                    });
                    return;
                }

                speechFile = new File(MP3_LOCATION);//speechFile保存后端语音
                if (!speechFile.exists()) speechFile.createNewFile();
                OutputStream outputStream = new FileOutputStream(speechFile);

                resultStream.writeTo(outputStream);

                getSpeechDone = true;
                if (getBgmDone) {
                    getSpeechDone = false;
                    getBgmDone = false;

                    if(firstIn) {
                        firstIn = false;
                        if(EditChapterActivity.this.isFinishing()) return;
                        EditChapterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingView.setVisibility(View.INVISIBLE);
                                normal.setVisibility(View.VISIBLE);
                            }
                        });
                    }else {
                        updateUiAfterTrans();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class DeleteSpeech extends Thread{
        private String path;

        public DeleteSpeech(String path){
            this.path = path;
        }

        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/deleteSpeech?path=" + path;

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
