package com.example.myapplication.Activity.Work;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.GetServer;
import com.example.myapplication.HttpUtils;
import com.example.myapplication.MilliToHMS;
import com.example.myapplication.MyToast;

import com.example.myapplication.R;

import com.example.myapplication.GetAudioLength;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class NewChapterActivity extends AppCompatActivity {

    private LinearLayout normal;
    private LinearLayout loadView;
    private SeekBar seekBar;//进度条
    private boolean textChanged = false;
    private boolean speechChanged = false;//同步音频与文本

    private int bookid;
    private String chapterTitle;
    private String content;
    private String speechPath;

    private File speechFile = null;
    private final String MP3_LOCATION = Environment.getExternalStorageDirectory().getPath()+"/temp.mp3";

    private MediaPlayer player;//音频播放

    private boolean firtstPlay = true;//是否首次播放当前音频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chapter);

        normal = findViewById(R.id.normal);
        loadView = findViewById(R.id.Loading);

        EditText content = findViewById(R.id.content);
        content.addTextChangedListener(watcher);
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
                    player.seekTo(seekBar.getProgress());

                    TextView begin = findViewById(R.id.begin);
                    MilliToHMS milliToHMS = new MilliToHMS();
                    begin.setText(milliToHMS.milliToHMS(seekBar.getProgress()));
                }
            }
        });//实现拖动进度条，调整播放进度

        normal.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        bookid = intent.getIntExtra("bookid",-1);

        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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

    @Override
    public void onBackPressed(){
        //保存草稿
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("保存草稿");
        builder.setMessage("是否保存为草稿?");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try{
                    //保存草稿数据至后端
                    new Thread(storeDraft).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new Thread(deleteDraft).start();
            }
        });

        builder.show();
    }

    public void onBackPressed(View view){
        onBackPressed();
    }

    public void refresh(View view){
        refresh();
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

        loadView.setVisibility(View.VISIBLE);//加载画面
        findViewById(R.id.loadinggif).setVisibility(View.VISIBLE);
        findViewById(R.id.Remind).setVisibility(View.INVISIBLE);

        new Thread(getDraft).start();
    }

    //保存章节
    public void storeChapter(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View v = LayoutInflater.from(this).inflate(R.layout.dialog_layout,null);
        TextView titleView = v.findViewById(R.id.Title);
        titleView.setText("标题");

        EditText text = findViewById(R.id.content);
        content = text.getText().toString();

        builder.setView(v);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText title = v.findViewById(R.id.Edit);
                chapterTitle = title.getText().toString();

                if(chapterTitle.length() > 20){//标题不能超过20个字
                    new MyToast(NewChapterActivity.this,getResources().getString(R.string.titlelong));
                    return;
                }

                speechPath = System.currentTimeMillis() + ".mp3";

                StoreChapter storeChapter = new StoreChapter(chapterTitle,bookid,content,speechPath);
                new Thread(storeChapter).start();
            }
        });

        builder.setNegativeButton("取消", null);

        builder.show();
    }

    public void textToSpeech(View view){
      new Thread(textToSpeech).start();
    }

    //重置播放状态
    private void resetPlayer(){
        player.reset();

        ImageView playButton = findViewById(R.id.PlayButton);
        playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));

        firtstPlay = true;
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
            if(!player.isPlaying()) {

                //首次播放设置数据源
                if(firtstPlay) {
                    seekBar.setProgress(0);
                    player.setDataSource(MP3_LOCATION);
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();//数据源类型
                    player.setAudioAttributes(audioAttributes);

                    player.prepareAsync();//异步准备音源
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            seekBar.setMax(player.getDuration());

                            //让进度条与播放进度同步
                            Timer timer = new Timer();
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    if(!player.isPlaying()) return;

                                    seekBar.setProgress(player.getCurrentPosition());

                                    normal.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            TextView begin = findViewById(R.id.begin);
                                            MilliToHMS milliToHMS = new MilliToHMS();
                                            begin.setText(milliToHMS.milliToHMS(player.getCurrentPosition()));
                                        }
                                    });

                                }
                            };
                            timer.schedule(task,0,10);

                            player.start();
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
                    player.start();
                }
            }

            //暂停
            else {
                player.pause();
                ImageView playButton = findViewById(R.id.PlayButton);
                playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.play));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Runnable textToSpeech = new Runnable() {
        @Override
        public void run() {
            try{

                //重置播放状态
                normal.post(new Runnable() {
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
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new MyToast(NewChapterActivity.this, getResources().getString(R.string.translateSuccess));

                            LinearLayout translating = findViewById(R.id.translating);
                            translating.setVisibility(View.INVISIBLE);

                            firtstPlay = true;

                            speechFile = new File(MP3_LOCATION);//speechFile保存后端语音
                            if (!speechFile.exists()) speechFile.createNewFile();
                            OutputStream outputStream = new FileOutputStream(speechFile);


                            resultStream.writeTo(outputStream);
                            speechChanged = true;

                            GetAudioLength getAudioLength = new GetAudioLength();
                            TextView end = findViewById(R.id.end);
                            end.setText(getAudioLength.getLength(speechFile));

                            seekBar.setProgress(0);

                            TextView begin = findViewById(R.id.begin);
                            begin.setText(getResources().getString(R.string.initial));
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

    Runnable deleteDraft = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/deleteDraft?bookid=" + bookid;

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(null, "GET", "application/json");//向后端发送请求

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        NewChapterActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable storeDraft = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/storeDraft";

                EditText text = findViewById(R.id.content);
                JSONObject info = new JSONObject();
                info.put("bookid",bookid);
                info.put("draft",text.getText().toString());
                byte[] param = info.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "GET", "application/json");//向后端发送请求

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(NewChapterActivity.this,getResources().getString(R.string.draft));
                        NewChapterActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //获取上次的草稿
    Runnable getDraft = new Runnable() {
        @Override
        public void run() {
            try{
                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/getDraft?bookid=" + bookid;

                HttpUtils httpUtils = new HttpUtils(url);
                ByteArrayOutputStream outputStream = httpUtils.doHttp(null, "GET",
                        "application/json");

                if (outputStream == null) {//请求超时
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterActivity.this, getResources().getString(R.string.HttpTimeOut));

                            findViewById(R.id.loadinggif).setVisibility(View.INVISIBLE);
                            findViewById(R.id.Remind).setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                final String draft = new String(outputStream.toByteArray(),
                        StandardCharsets.UTF_8);

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText text = findViewById(R.id.content);
                        text.setText(draft);

                        loadView.setVisibility(View.INVISIBLE);
                        normal.setVisibility(View.VISIBLE);
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

        public StoreChapter(String title,int bookid,String content,String speechPath){
            this.title = title;
            this.bookid = bookid;
            this.content = content;
            this.speechPath = speechPath;
        }

        @Override
        public void run() {
            try{

                if(textChanged && !speechChanged){
                    normal.post(new Runnable() {
                        @Override
                        public void run() {
                            new MyToast(NewChapterActivity.this,getResources().getString(R.string.askpush));
                        }
                    });
                    return;
                }

                GetServer getServer = new GetServer();
                String url = getServer.getIPADDRESS()+"/audiobook/storeChapter";

                JSONObject info = new JSONObject();
                info.put("title",title);
                info.put("bookid",bookid);
                info.put("content",content);
                info.put("speechPath",speechPath);

               GetAudioLength getAudioLength = new GetAudioLength();
               info.put("length",getAudioLength.getLength(speechFile));

                byte[]param = info.toString().getBytes();

                HttpUtils httpUtils = new HttpUtils(url);
                httpUtils.doHttp(param, "POST", "application/json");

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        new Thread(storeSpeech).start();
                    }
                });
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

                normal.post(new Runnable() {
                    @Override
                    public void run() {
                        new MyToast(NewChapterActivity.this,"创建成功!");
                        speechFile.delete();
                        NewChapterActivity.super.onBackPressed();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

}
