package com.cpd.soundbook.AudioUtils;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component(value = "getMP3Length")
public class GetMP3Length {
    final private String PYTHON_PATH = "python";//D:\python\python.exe
    final private String EXE_PATH ="/audiobook/MP3Length.py";//D:\AudiobookBack\MP3Length.py

    public String getLength(String filePath){
        String result="";
        try{
            String[] arguments = new String[]{PYTHON_PATH,EXE_PATH,filePath};
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"GBK"));
            String line;
            while ((line = reader.readLine()) != null){
                result = line;
            }
            reader.close();

            int resultCode = process.waitFor();
            System.out.println("python get MP3Length execute " + resultCode + " 0(成功)1(失败)");
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
