package com.cpd.soundbook.AudioUtils;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;


//合成文本长度必须小于1024字节，如果本文长度较长，可以采用多次请求的方式。切忌文本长度超过限制。
//百度API还有QPS限制，当请求失败时，重新发送请求
@Component(value = "tts")
public class TextToSpeech {

    private static final String APP_ID="16704195";
    private static final String API_KEY="F5aW7RA96u6GyzIG2tzwoZNP";
    private static final String SECRET_KEY="YnpKVuzxdEaOeWq9Bt5x6tUven3BvggP";//百度API调用所需APPID、APPKEY、SECRETKEY

    final private int INFINITE = 999999999;

    private static AipSpeech instance = null;
    public static synchronized AipSpeech getInstance(){
        if(instance == null){
            instance = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
        }

        return instance;
    }

    public File translate(String text,String tempDir){

        try{

            RandomName randomName = new RandomName();

            String path = tempDir + randomName.randomName() + ".mp3";
            File result = new File(path);
            if(!result.exists()){
                result.createNewFile();
            }

            //百度API
            AipSpeech client = getInstance();
            client.setConnectionTimeoutInMillis(INFINITE);
            client.setSocketTimeoutInMillis(INFINITE);

            HashMap<String,Object> options = new HashMap<String,Object>();
            options.put("spd","4");//语速
            options.put("per","3");//发音人
            options.put("pit","3");//语调
            OutputStream outputStream = new FileOutputStream(result);

            TtsResponse res = client.synthesis(text, "zh", 1, options);//中文

            /*if (res.getResult() != null) {
                /*System.out.println("thread: " + Thread.currentThread().getName() + "sleep");
                Thread.sleep(200);
                System.out.println("thread: " + Thread.currentThread().getName() + "wake");

                return null;
            }*/


            byte[] data = res.getData();
            if (data != null) {
                try {
                    outputStream.write(data, 0, data.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            outputStream.close();

            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
