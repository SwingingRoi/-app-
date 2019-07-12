package com.cpd.soundbook.AudioUtils;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Component(value = "addEffect")
public class AddEffect {


    private final String FFMPEG_PATH = "D:\\ffmepg\\ffmepg\\bin\\ffmpeg.exe";//ffmpeg执行路径

    @Autowired
    private RandomName randomName;

    public File addEffect(String text){

        List<String> paths = new ArrayList<>();
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
            //首先将text转化为语音speech,plainSpeechLength为speech的时长
            TextToSpeech textToSpeech = new TextToSpeech();
            File speech = textToSpeech.translate(text);
            paths.add(speech.getAbsolutePath());
            MP3File plainSpeech = (MP3File)AudioFileIO.read(speech);
            MP3AudioHeader plainSpeechAudioHeader = (MP3AudioHeader) plainSpeech.getAudioHeader();
            int plainSpeechLength = plainSpeechAudioHeader.getTrackLength();

            int index = text.indexOf('雨');//关键词
            int rate = plainSpeechLength * index / text.length();//确定在何处分割音频

            split("00:00:00", String.valueOf(rate), speech.getAbsolutePath(),
                        tempPath1);

            split(String.valueOf(rate),String.valueOf(plainSpeechLength + 100),speech.getAbsolutePath(),
                    tempPath2);

            volumeDown(System.getProperty("user.dir")+"\\"+"mp3\\effectSpeech\\rain.mp3",
                    downEffectPath);

            compose(tempPath2, downEffectPath, tempEffectPath);//合成后半部分和音效

            String path1 = "file " + "\'" + tempPath1 + "\'" + System.getProperty("line.separator");
            String path2 = "file " + "\'" + tempEffectPath + "\'" + System.getProperty("line.separator");
            PrintWriter writer = new PrintWriter(txtPath);

            if(rate > 0 && rate < plainSpeechLength){
                writer.write(path1);
                writer.write(path2);
                writer.flush();
                writer.close();

                file = concat(txtPath, resultPath);
            }
            else if(rate == 0){
                writer.write(path2);
                writer.flush();
                writer.close();

                file = concat(txtPath, resultPath);
            }
            else if(rate == plainSpeechLength){
                writer.write(path1);
                writer.flush();
                writer.close();

                file = concat(txtPath, resultPath);
            }

            deleteTempFile(paths);
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

    //分割音频
    //ffmpeg -i input -vn -acodec copy -ss from -t to output
    private void split(String from,String to,String srcF,String desF){
        try {
            File file = new File(desF);
            if (file.exists()) file.delete();

            List<String> commands = new ArrayList<>();
            commands.add(FFMPEG_PATH);
            commands.add("-i");
            commands.add(srcF);
            commands.add("-vn");
            commands.add("-acodec");
            commands.add("copy");
            commands.add("-ss");
            commands.add(from);
            commands.add("-t");
            commands.add(to);
            commands.add(desF);
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            process.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //降低音量
    //ffmpeg -i input -af volume=?dB output
    private void volumeDown(String srcF,String desF){
        try{
            File file = new File(desF);
            if(file.exists()) file.delete();

            List<String> commands = new ArrayList<>();
            commands.add(FFMPEG_PATH);
            commands.add("-i");
            commands.add(srcF);
            commands.add("-af");
            commands.add("volume=-1dB");
            commands.add(desF);

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            process.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //合成语音和音效
    //ffmpeg -i input1 -i input2 -filter_complex amix=inputs=2:duration=first output
    private void compose(String srcF1,String srcF2,String desF){
        try{
            File file = new File(desF);
            if(file.exists()) file.delete();

            List<String> commands = new ArrayList<>();
            commands.add(FFMPEG_PATH);
            commands.add("-i");
            commands.add(srcF1);
            commands.add("-i");
            commands.add(srcF2);
            commands.add("-filter_complex");
            commands.add("amix=inputs=2:duration=first");
            commands.add(desF);
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            process.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //拼接音频
    //ffmpeg -f concat -safe 0 -i input.txt -c copy output
    public File concat(String src,String desF){
        File file = new File(desF);
        try{
            if (file.exists()) file.delete();

            List<String> commands = new ArrayList<>();
            commands.add(FFMPEG_PATH);
            commands.add("-f");
            commands.add("concat");
            commands.add("-safe");
            commands.add("0");
            commands.add("-i");
            commands.add(src);
            commands.add("-c");
            commands.add("copy");
            commands.add(desF);
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            process.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

}
