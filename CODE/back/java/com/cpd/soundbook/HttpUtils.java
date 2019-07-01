package com.cpd.soundbook;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Component(value = "httpUtils")
public class HttpUtils {
    public String getStringParam(HttpServletRequest request){
        String result="";
        try {
            request.setCharacterEncoding("UTF-8");
            InputStream param = request.getInputStream();
            result = IOUtils.toString(param,"UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public File getFileParam(HttpServletRequest request){
        File file = null;
        try{
            InputStream param = request.getInputStream();
            String path = System.getProperty("user.dir")+"\\"+System.currentTimeMillis()+".png";
            file = new File(path);
            if(!file.exists()){
                file.createNewFile();
            }
            OutputStream outputStream = new FileOutputStream(file);
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = param.read(buffer, 0, 8192)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
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
}
