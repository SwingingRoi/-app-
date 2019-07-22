package com.cpd.soundbook;

import com.baidu.aip.speech.AipSpeech;
import it.sauronsoftware.jave.EncoderException;
import org.json.JSONArray;
import org.json.JSONObject;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;

import java.io.*;

public class Audio2Text {
    //设置APPID/AK/SK
    private static final String APP_ID = "16840366";
    private static final String API_KEY = "cgKsQvXGPiHwdw0RnqQWVVGE";
    private static final String SECRET_KEY = "jmkD1r807v1zbDAbsWRS9hSN8iRmxgC7";

    private static int mp3CutOneForAll(File file,String localpath){
        Encoder encoder = new Encoder();
        int bit = 0, length = 0;

        try{
            MultimediaInfo multimediaInfo = encoder.getInfo(file);
            bit = multimediaInfo.getAudio().getBitRate();
            length = (int)multimediaInfo.getDuration() / 1000;
            System.out.println("bitRate:" + bit);
            System.out.println("length:" + length);
        } catch (EncoderException e){
            e.printStackTrace();
        }

        int i;
        //更改裁剪间隔修改这个参数
        int duration=30;

        for(i = 0; duration * i < length - duration + 1; i++){

            BufferedOutputStream bos = null;

            int start = bit * duration * i * 1024 / 8, end = bit * duration * (i+1) * 1024 / 8;
            File targetFile = new File(localpath + i +".mp3");

            try{
                bos = new BufferedOutputStream(new FileOutputStream(targetFile, true));
                cutMusic(file, start, end, bos);
            }catch (IOException e){
                e.printStackTrace();
            }finally{
                try {
                    if(bos != null) bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if(duration * i > length - duration){

            BufferedOutputStream bos = null;

            int start = bit * duration * i * 1024 / 8, end = bit * length * 1024 / 8;
            File targetFile = new File(localpath + i +".mp3");

            try{
                bos = new BufferedOutputStream(new FileOutputStream(targetFile, true));
                cutMusic(file, start, end, bos);
                System.out.println(targetFile.getName());
                System.out.println(i);
                System.out.println("总共音频数目：" + (i + 1));
            }catch (IOException e){
                e.printStackTrace();
            }finally{
                try {
                    if(bos != null) bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return i+1;
    }

    private static void cutMusic(File file, int start, int end, BufferedOutputStream bos){

        BufferedInputStream bis = null;
        int len, total = 0;
        byte[] bytes = new byte[512];

        try{
            bis = new BufferedInputStream(new FileInputStream(file));
            while((len = bis.read(bytes)) != -1){
                total += len;
                if(total < start){
                    continue;
                }
                bos.write(bytes);   //写入的都是在我们预先指定的字节范围之内
                if(total >= end){  //当tatol的值超过预先设定的范围，则立刻刷新bos流对象，并结束循环
                    bos.flush();
                    break;
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try{
                if(bis != null){
                    bis.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static void deleteAllFilesOfDir(File path) {
        if (!path.exists())
            return;
        if (path.isFile()) {
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAllFilesOfDir(files[i]);
        }
        path.delete();
    }

    public static String audio2text(File file){
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
        String result="";

        //@localstore:本地用来存放裁剪的音频和转换的pcm的文件夹，在函数执行完毕后会删除
        String localpath="C://Users/jhd/Desktop/test/";
        File localstore=new File(localpath);
        localstore.mkdir();

        //@num:裁完的音频数,要根据性能修改裁剪时间时改上面的函数里的数字，现在默认为30秒，改时把30改成需要的就行
        int num=mp3CutOneForAll(file,localpath);

        for(int i=0;i<num;i++){
            String path1=localpath+i+".mp3";
            String path2=localpath+i+".pcm";
            try{
                //执行cmd窗口命令
                String cmdstr=String.format("cmd /c ffmpeg -y -i %s -acodec pcm_s16le -f s16le -ac 1 -ar 16000 %s",path1,path2);
                Process process=Runtime.getRuntime().exec(cmdstr);
                //等待执行完毕，不然会报找不到文件的错
                process.waitFor();

                JSONObject res = client.asr(path2, "pcm", 16000, null);
                JSONArray r=(JSONArray) res.get("result");
                result+=r.get(0).toString();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        deleteAllFilesOfDir(localstore);
        return result;
    }

    public static void main(String[] args) throws Exception{
        System.out.print(audio2text(new File("C://Users/jhd/Desktop/test.mp3")));
    }
}
