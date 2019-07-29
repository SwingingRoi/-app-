package com.cpd.soundbook.AudioUtils;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.MultimediaInfo;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;

@Component(value = "audioUtils")
public class AudioUtils {
    final private int duration = 60;//裁剪间隔

    //file:待切割文件
    //localPath:切割后文件存储的目录
    private int mp3CutOneForAll(File file, String localpath){
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

    private void cutMusic(File file, int start, int end, BufferedOutputStream bos){

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
                if(total >= end){  //当total的值超过预先设定的范围，则立刻刷新bos流对象，并结束循环
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

    //删除临时目录
    public void deleteAllFilesOfDir(File tempDir) {
        if (!tempDir.exists())
            return;
        if (tempDir.isFile()) {
            tempDir.delete();
            return;
        }
        File[] files = tempDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAllFilesOfDir(files[i]);
        }
        tempDir.delete();
    }

    /*返回值<目录名,目录文件数>
    目录下文件名为:目录名0，目录名1...
     */
    public HashMap<String,Integer> cutFile(File file){
        RandomName randomName = new RandomName();
        HashMap<String,Integer> result = new HashMap<>();
        try{
            String tempDir = System.getProperty("user.dir") + "\\" + randomName.randomName()+ "\\";
            File localStore = new File(tempDir);
            if(!localStore.exists()) localStore.mkdir();

            int num = mp3CutOneForAll(file,tempDir);

            result.put(tempDir,num);
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
