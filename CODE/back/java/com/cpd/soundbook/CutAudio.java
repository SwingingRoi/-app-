package com.cpd.soundbook;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component(value = "cutAudio")
public class CutAudio {
    final private String PYTHON_PATH = "D:\\python\\python.exe";
    final private String EXE_PATH = "D:\\AudiobookBack\\Audiocut.py";

    /*
    @srcFile:待切割文件文件路径
    @tempDir:存储临时切割文件的目录
     */
    public List<String> cutAudio(String srcFile,String tempDir){
        List<String> result = new ArrayList<>();
        try{
            String[] arguments = new String[]{PYTHON_PATH,EXE_PATH,srcFile,tempDir};
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"GBK"));
            String line;
            while ((line = reader.readLine()) != null){
                result.add(line);
                //System.out.println(line);
            }
            reader.close();

            int resultCode = process.waitFor();
            System.out.println("python execute " + resultCode + " 0(成功)1(失败)");
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args){
        try{
            String[] arguments = new String[]{"D:\\python\\python.exe","D:\\AudiobookBack\\Audiocut.py","C:\\Users\\09\\Desktop\\test\\test.mp3","C:\\Users\\09\\Desktop\\hello\\"};
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"GBK"));
            String line;
            while ((line = reader.readLine()) != null){
                System.out.println(line);
            }
            reader.close();

            int resultCode = process.waitFor();
            System.out.println(resultCode);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
