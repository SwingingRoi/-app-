package com.cpd.soundbook.AudioUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import com.iflytek.msp.cpdb.lfasr.client.LfasrClientImp;
import com.iflytek.msp.cpdb.lfasr.exception.LfasrException;
import com.iflytek.msp.cpdb.lfasr.model.LfasrType;
import com.iflytek.msp.cpdb.lfasr.model.Message;
import com.iflytek.msp.cpdb.lfasr.model.ProgressStatus;


import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;

/*
@Component(value = "audioToText")

public class AudioToText {
    //设置APPID/AK/SK

    private static final String APP_ID = "17150236";
    private static final String API_KEY = "G9MKT50DV4cnScuojj62OiEX";
    private static final String SECRET_KEY = "9ldPntk9jZBlUHm9aiERseu5dIo5uSWa";


    //只支持pcm格式文件的转换
    public String audioToText(File srcFile){
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
            org.json.JSONArray r = (JSONArray) res.get("result");
            System.out.println(res.toString());
            return r.get(0).toString();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args){
        File file = new File("D:\\AudiobookBack\\mp3\\stt\\crRksW5sFLSSiQ0\\0.pcm");
        AudioToText audioToText = new AudioToText();
        System.out.println(audioToText.audioToText(file));
    }
}*/



@Component(value = "audioToText")
public class AudioToText{
    private static final LfasrType type = LfasrType.LFASR_STANDARD_RECORDED_AUDIO;


    public String audioToText(File srcFile){
        // 初始化LFASRClient实例
        LfasrClientImp lc;
        String text;

        try {
            lc = LfasrClientImp.initLfasrClient();
        } catch (LfasrException e) {
            // 初始化异常，解析异常描述信息
            Message initMsg = JSON.parseObject(e.getMessage(), Message.class);
            System.out.println("ecode=" + initMsg.getErr_no());
            System.out.println("failed=" + initMsg.getFailed());
            return null;
        }

        String task_id;
        HashMap<String, String> params = new HashMap<>();
        params.put("has_participle", "true");

        try {
            // 上传音频文件
            Message uploadMsg = lc.lfasrUpload(srcFile.getAbsolutePath(), type, params);

            // 判断返回值
            int ok = uploadMsg.getOk();
            if (ok == 0) {
                // 创建任务成功
                task_id = uploadMsg.getData();
            } else {
                // 创建任务失败-服务端异常
                System.out.println("ecode=" + uploadMsg.getErr_no());
                System.out.println("failed=" + uploadMsg.getFailed());
                return null;
            }
        } catch (LfasrException e) {
            // 上传异常，解析异常描述信息
            Message uploadMsg = JSON.parseObject(e.getMessage(), Message.class);
            System.out.println("ecode=" + uploadMsg.getErr_no());
            System.out.println("failed=" + uploadMsg.getFailed());
            return null;
        }

        System.out.println("task id " + task_id);
        // 循环等待音频处理结果
        while (true) {
            try {
                // 获取处理进度
                //System.out.println(1);
                Message progressMsg = lc.lfasrGetProgress(task_id);

                // 如果返回状态不等于0，则任务失败
                if (progressMsg.getOk() != 0) {
                    System.out.println("转写失败. task_id:" + task_id);
                    System.out.println("ecode=" + progressMsg.getErr_no());
                    System.out.println("failed=" + progressMsg.getFailed());
                    return null;
                } else {
                    ProgressStatus progressStatus = JSON.parseObject(progressMsg.getData(), ProgressStatus.class);
                    if (progressStatus.getStatus() == 9) {
                        // 处理完成,跳出打印结果
                        break;
                    }
                }
            } catch (LfasrException e) {
                // 获取进度异常处理，根据返回信息排查问题后，再次进行获取
                Message progressMsg = JSON.parseObject(e.getMessage(), Message.class);
                System.out.println("ecode=" + progressMsg.getErr_no());
                System.out.println("failed=" + progressMsg.getFailed());
                return null;
            }
        }

        // 获取任务结果
        try {
            Message resultMsg = lc.lfasrGetResult(task_id);
            // 如果返回状态等于0，则获取任务结果成功
            if (resultMsg.getOk() == 0) {
                // 打印转写结果
                com.alibaba.fastjson.JSONArray jsonArray= JSONArray.parseArray(resultMsg.getData());
                String last="";
                StringBuilder buf=new StringBuilder();
                for (int i=0;i<jsonArray.size();++i){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    String result=jsonObject.getString("onebest");
                    buf.append(result);
                }
                last=buf.toString();
                text = last;
                System.out.println(last);
            } else {
                // 获取任务结果失败
                System.out.println("ecode=" + resultMsg.getErr_no());
                System.out.println("failed=" + resultMsg.getFailed());
                return null;
            }
        } catch (LfasrException e) {
            // 获取结果异常处理，解析异常描述信息
            Message resultMsg = JSON.parseObject(e.getMessage(), Message.class);
            System.out.println("ecode=" + resultMsg.getErr_no());
            System.out.println("failed=" + resultMsg.getFailed());
            return null;
        }

        return text;
    }

    public static void main(String[] args){
        File file = new File("D:\\AudiobookBack\\mp3\\stt\\crRksW5sFLSSiQ0\\0.mp3");
        AudioToText audioToText = new AudioToText();
        System.out.println(audioToText.audioToText(file));
    }
}
