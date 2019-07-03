package com.cpd.soundbook;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

@RestController
public class Baidu {

    public static final String APPID="16704195";
    public static final String APIKEY="F5aW7RA96u6GyzIG2tzwoZNP";
    public static final String SECRETKEY="YnpKVuzxdEaOeWq9Bt5x6tUven3BvggP";


    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/test")
    public void test(HttpServletResponse response){
        try{

            AipSpeech client = new AipSpeech(APPID,APIKEY,SECRETKEY);
            client.setConnectionTimeoutInMillis(2000);
            client.setSocketTimeoutInMillis(60000);

            HashMap<String,Object> options = new HashMap<String,Object>();
            options.put("spd",5);
            options.put("per",0);
            TtsResponse res = client.synthesis("在这部作品中奥威尔刻画了一个令人感到窒息的恐怖世界，在假想的未来社会中，独裁者以追逐权力为最终目标，人性被强权彻底扼杀，自由被彻底剥夺，思想受到严酷钳制，人民的生活陷入了极度贫困，下层人民的人生变成了单调乏味的循环。", "zh", 1, options);
            byte[] data = res.getData();
            JSONObject res1 = res.getResult();
            if (data != null) {
                try {
                    //Util.writeBytesToFileSystem(data, "output.mp3");
                    String path = System.getProperty("user.dir")+"\\"+"output.mp3";
                    File file = new File(path);
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    OutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(data, 0, data.length);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (res1 != null) {
                System.out.println(res1.toString(2));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
