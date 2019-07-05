package com.cpd.soundbook;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

@Component(value = "tts")
public class TextToSpeech {

    private static final String APPID="16704195";
    private static final String APIKEY="F5aW7RA96u6GyzIG2tzwoZNP";
    private static final String SECRETKEY="YnpKVuzxdEaOeWq9Bt5x6tUven3BvggP";//百度API调用所需APPID、APPKEY、SECRETKEY


    public File translate(String text){

        try{

            File result = new File(System.getProperty("user.dir")+"\\"+"output.mp3");
            if(!result.exists()){
                result.createNewFile();
            }

            //百度API
            AipSpeech client = new AipSpeech(APPID,APIKEY,SECRETKEY);
            client.setConnectionTimeoutInMillis(2000);
            client.setSocketTimeoutInMillis(60000);

            HashMap<String,Object> options = new HashMap<String,Object>();
            options.put("spd",4);//语速
            options.put("per",3);//发音人
            TtsResponse res = client.synthesis(text, "zh", 1, options);
            byte[] data = res.getData();
            JSONObject res1 = res.getResult();
            if (data != null) {
                try {
                    OutputStream outputStream = new FileOutputStream(result);
                    outputStream.write(data, 0, data.length);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (res1 != null) {
                System.out.println(res1.toString(2));
            }

            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
