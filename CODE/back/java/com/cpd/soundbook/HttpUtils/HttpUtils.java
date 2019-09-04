package com.cpd.soundbook.HttpUtils;

import com.cpd.soundbook.AudioUtils.RandomName;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Component(value = "httpUtils")
public class HttpUtils {
    @Autowired
    private RandomName randomName;

    public String getStringParam(HttpServletRequest request){
        String result="";
        try {
            request.setCharacterEncoding("UTF-8");
            InputStream param = request.getInputStream();
            result = IOUtils.toString(param,"UTF-8");
            param.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private File createFile(String path,InputStream inputStream){
        File file = new File(path);
        try{
            if (!file.exists()) file.createNewFile();

            OutputStream outputStream = new FileOutputStream(file);
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public File getPngParam(HttpServletRequest request){
        File file = null;
        try{
            InputStream param = request.getInputStream();
            //System.getProperty("user.dir")+"\\" + randomName.randomName() +".png"
            //"/audiobook/" + randomName.randomName() +".png"
            String path = "/audiobook/" + randomName.randomName() +".png";
            file = createFile(path,param);
            param.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public File getMp3Param(HttpServletRequest request){
        File file = null;
        try{
            InputStream param = request.getInputStream();
            //System.getProperty("user.dir")+"\\" + randomName.randomName() +".mp3"
            //"/audiobook/" + randomName.randomName() +".mp3"
            String path = "/audiobook/" + randomName.randomName() +".mp3";
            file = createFile(path,param);
            param.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public void writeStringBack(HttpServletResponse response,String data){
        try{
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print(data);
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void writeFileBack(HttpServletResponse response,File file){
        try {
            OutputStream outputStream = response.getOutputStream();
            InputStream inputStream = new FileInputStream(file);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
