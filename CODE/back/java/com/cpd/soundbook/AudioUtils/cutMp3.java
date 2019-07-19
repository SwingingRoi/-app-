package cutMP3;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.MultimediaInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class cutMp3 {

    public static void main(String[] args) {
        cutMp3 t = new cutMp3();

        //f1,f2分别为需要剪切的歌曲路径
        File f1 = new File("E:\\CutMusicTest\\1.mp3");
        File f2 = new File("E:\\CutMusicTest\\2.mp3");

        //f为合并的歌曲
        File f = new File("E:\\CutMusicTest\\MergeMusic.mp3");

        t.oneForAll(f2);
        t.allForOne(f1,f2,f);
    }

    public void oneForAll(File file){

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

        for(i = 0; 10 * i < length - 9; i++){

            BufferedOutputStream bos = null;

            int start = bit * 10 * i * 1024 / 8, end = bit * 10 * (i+1) * 1024 / 8;
            File targetFile = new File("E:\\CutMusicTest\\targetMusics\\target" + i +".mp3");

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

        if(10 * i > length - 10){

            BufferedOutputStream bos = null;

            int start = bit * 10 * i * 1024 / 8, end = bit * length * 1024 / 8;
            File targetFile = new File("E:\\CutMusicTest\\targetMusics\\target" + i +".mp3");

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

    }

    public void allForOne(File f1,File f2,File f){

        //get bitRate
        Encoder encoder = new Encoder();
        int bit1 = 0, bit2 = 0;
        try{
            MultimediaInfo multimediaInfo = encoder.getInfo(f1);
            bit1 = multimediaInfo.getAudio().getBitRate();
            multimediaInfo = encoder.getInfo(f2);
            bit2 = multimediaInfo.getAudio().getBitRate();
            System.out.println("bitRate1:" + bit1);
            System.out.println("bitRate2:" + bit2);
        } catch (EncoderException e){
            e.printStackTrace();
        }

        BufferedOutputStream bos = null;

        //第一首歌剪切部分起始字节
        int start1 = bit1 * 67 * 1024 / 8;
        int end1 = bit1 * 74 * 1024 / 8;

        //第二首歌剪切部分起始字节，计算方式同上
        int start2 = 0;
        int end2 = bit2 * 18 * 1024 / 8;

        try {
            //缓冲字节输出流（true表示可以在流的后面追加数据，而不是覆盖！！）
            bos = new BufferedOutputStream(new FileOutputStream(f,true));

            cutMusic(f1, start1, end1, bos);
            cutMusic(f2, start2, end2, bos);

        } catch (IOException e) {

            e.printStackTrace();

        }finally{
            try {
                if(bos != null) {
                    bos.close();
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
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

                if(total >= end){  //当tatol的值超过预先设定的范围，则立刻刷新bos流对象，并结束循环
                    bos.flush();
                    break;
                }
            }
            System.out.println("剪切完成！");

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

}