package com.cpd.soundbook;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;


//合成文本长度必须小于1024字节，如果本文长度较长，可以采用多次请求的方式。切忌文本长度超过限制。
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
            options.put("spd","4");//语速
            options.put("per","3");//发音人
            options.put("pit","3");//语调
            OutputStream outputStream = new FileOutputStream(result);

            int preIndex=0;
            for(int i=0;i<text.length();i++) {

                String textNow="";

                if(text.charAt(i) == '。' || i==text.length() - 1){
                    textNow = text.substring(preIndex,i+1);
                    preIndex = preIndex + textNow.length();
                    TtsResponse res = client.synthesis(textNow, "zh", 1, options);//中文
                    byte[] data = res.getData();
                    if (data != null) {
                        try {
                            outputStream.write(data, 0, data.length);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            //System.out.println(textNow);
            }
            outputStream.close();

            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
