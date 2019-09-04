package com.cpd.soundbook.AudioUtils;

import com.cpd.soundbook.MongoDB.MongoDBInter;
import com.mongodb.gridfs.GridFSDBFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

@Component(value = "addEffect")
public class AddEffect {

    @Autowired
    private FfmpegUtils ffmpegUtils;


    @Autowired
    private RandomName randomName;

    @Autowired
    private GetEffectKey getEffectKey;

    @Autowired
    private MongoDBInter mongoDAO;

    public File addEffect(File srcFile,String text){

        List<String> paths = new ArrayList<>();
        //存储音效的临时文件
        String effectPath = System.getProperty("user.dir") + "\\mp3\\effectSpeech\\" + randomName.randomName() + ".mp3";
        paths.add(effectPath);

        //将text从关键词处分割为两部分
        //tempPath1:存储前半部分
        String tempPath1 = System.getProperty("user.dir") + "\\mp3\\effectSpeech\\" + randomName.randomName() + ".mp3";
        paths.add(tempPath1);

        //tempPath2:存储后半部分
        String tempPath2 = System.getProperty("user.dir") + "\\mp3\\effectSpeech\\" + randomName.randomName() + ".mp3";
        paths.add(tempPath2);

        //downEffectPath:存储降低音量后的音效文件
        String downEffectPath = System.getProperty("user.dir") + "\\mp3\\effectSpeech\\" + randomName.randomName()+ ".mp3";
        paths.add(downEffectPath);

        //tempEffectPath:存储后半部分和音效的合成文件
        String tempEffectPath = System.getProperty("user.dir") + "\\mp3\\effectSpeech\\" + randomName.randomName() + ".mp3";
        paths.add(tempEffectPath);

        //txtPath:存储ffmpeg执行MP3文件合并所需文件参数
        String txtPath = System.getProperty("user.dir") + "\\mp3\\effectSpeech\\" + randomName.randomName() + ".txt";
        paths.add(txtPath);

        //resultPath:存储最后的合成结果
        String resultPath = System.getProperty("user.dir") + "\\mp3\\effectSpeech\\" + randomName.randomName() + ".mp3";

/*
        System.out.println("tempPath1: " + tempPath1);
        System.out.println("tempPath2: " + tempPath2);
        System.out.println("downEffectPath: " + downEffectPath);
        System.out.println("tempEffectPath: " + tempEffectPath);
        System.out.println("txtPath: " + txtPath);
        System.out.println("resultPath: " + resultPath);
*/
        File file = null;
        try{
            //plainSpeechLength为srcFile的时长
            MP3File plainSpeech = (MP3File)AudioFileIO.read(srcFile);
            MP3AudioHeader plainSpeechAudioHeader = (MP3AudioHeader) plainSpeech.getAudioHeader();
            int plainSpeechLength = plainSpeechAudioHeader.getTrackLength();

            HashMap<Integer,String> divideResult = getEffectKey.getKeyList(text);

            Iterator<Map.Entry<Integer,String>> entryIterator = divideResult.entrySet().iterator();

            if(divideResult.size() != 0) {
                Map.Entry<Integer,String> entry = entryIterator.next();
                int rate = plainSpeechLength * entry.getKey() / text.length();//确定在何处分割音频

                //确定选取什么音效
                GridFSDBFile gridFSDBFile = mongoDAO.getEffectByName(entry.getValue());
                OutputStream outputStream = new FileOutputStream(effectPath);
                gridFSDBFile.writeTo(outputStream);

                ffmpegUtils.split("00:00:00", String.valueOf(rate), srcFile.getAbsolutePath(),
                        tempPath1);

                ffmpegUtils.split(String.valueOf(rate), String.valueOf(plainSpeechLength + 100), srcFile.getAbsolutePath(),
                        tempPath2);


                ffmpegUtils.volumeDown(effectPath, downEffectPath);

                ffmpegUtils.compose(tempPath2, downEffectPath, tempEffectPath);//合成后半部分和音效

                String path1 = "file " + "\'" + tempPath1 + "\'" + System.getProperty("line.separator");
                String path2 = "file " + "\'" + tempEffectPath + "\'" + System.getProperty("line.separator");
                PrintWriter writer = new PrintWriter(txtPath);

                if (rate > 0 && rate < plainSpeechLength) {
                    writer.write(path1);
                    writer.write(path2);
                    writer.flush();
                    writer.close();

                    file = ffmpegUtils.concat(txtPath, resultPath);
                } else if (rate == 0) {
                    writer.write(path2);
                    writer.flush();
                    writer.close();

                    file = ffmpegUtils.concat(txtPath, resultPath);
                } else if (rate == plainSpeechLength) {
                    writer.write(path1);
                    writer.flush();
                    writer.close();

                    file = ffmpegUtils.concat(txtPath, resultPath);
                }

                outputStream.close();
                deleteTempFile(paths);
            }else {
                file = srcFile;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    //清除临时文件
    private void deleteTempFile(List<String> paths){
        try{
            for(String path : paths){
                File file = new File(path);
                if(file.exists()){
                    file.delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
