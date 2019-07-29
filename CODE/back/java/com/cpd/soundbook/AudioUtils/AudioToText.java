package com.cpd.soundbook.AudioUtils;

import com.baidu.aip.speech.AipSpeech;
import it.sauronsoftware.jave.EncoderException;
import org.json.JSONArray;
import org.json.JSONObject;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(value = "audioToText")
public class AudioToText {
    //设置APPID/AK/SK

    private static final String APP_ID = "16840366";
    private static final String API_KEY = "cgKsQvXGPiHwdw0RnqQWVVGE";
    private static final String SECRET_KEY = "jmkD1r807v1zbDAbsWRS9hSN8iRmxgC7";

    //只支持pcm格式文件的转换
    public  String audioToText(File srcFile){
        try {
            // 初始化一个AipSpeech
            AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);

            // 可选：设置网络连接参数
            //client.setConnectionTimeoutInMillis(2000);
            //client.setSocketTimeoutInMillis(60000);

            // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
            //client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
            //client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

            // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
            // 也可以直接通过jvm启动参数设置此环境变量
            //System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");

            // 调用接口
            //@path:上传的源文件路径 @file:音频文件 @result:返回值


            //@localstore:本地用来存放裁剪的音频和转换的pcm的文件夹，在函数执行完毕后会删除

            JSONObject res = client.asr(srcFile.getAbsolutePath(), "pcm", 16000, null);
            //System.out.println(res.toString());
            JSONArray r = (JSONArray) res.get("result");
            return r.get(0).toString();
        }catch (Exception e){
            //e.printStackTrace();
            return null;
        }
    }
}
