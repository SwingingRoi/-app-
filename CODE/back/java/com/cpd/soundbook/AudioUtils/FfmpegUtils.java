package com.cpd.soundbook.AudioUtils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component(value = "ffmpegUtils")
public class FfmpegUtils {
    private final String FFMPEG_PATH = "D:\\ffmpeg\\ffmpeg\\bin\\ffmpeg.exe";//ffmpeg执行路径

    //分割音频
    //ffmpeg -i input -vn -acodec copy -ss from -t to output
    public void split(String from,String to,String srcF,String desF){
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
    public void volumeDown(String srcF,String desF){
        try{
            File file = new File(desF);
            if(file.exists()) file.delete();

            List<String> commands = new ArrayList<>();
            commands.add(FFMPEG_PATH);
            commands.add("-i");
            commands.add(srcF);
            commands.add("-af");
            commands.add("volume=20dB");
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
    public void compose(String srcF1,String srcF2,String desF){
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
            commands.add("volume=2[a0];volume=0.1[a1];[a0][a1]amix=inputs=2:duration=first");
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
            if(file.exists()) file.delete();
            //System.out.println("concat begin");
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
            //System.out.println("concat done");
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public void transformFormat(String src,String dest){
        try{
            //执行cmd窗口命令
            List<String> commands = new ArrayList<>();
            commands.add(FFMPEG_PATH);
            commands.add("-y");
            commands.add("-i");
            commands.add(src);
            commands.add("-acodec");
            commands.add("pcm_s16le");
            commands.add("-f");
            commands.add("s16le");
            commands.add("-ac");
            commands.add("1");
            commands.add("-ar");
            commands.add("16000");
            commands.add(dest);

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            process.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
