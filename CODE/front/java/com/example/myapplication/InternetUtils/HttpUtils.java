package com.example.myapplication.InternetUtils;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    private URL url;
    private final int TIMEOUT=5000;

    public HttpUtils(String url){
        try {
            this.url = new URL(url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream doHttp(byte[] param,String method,String contentType){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type",contentType);
            if(param!=null) {
                OutputStream ostream = connection.getOutputStream();
                ostream.write(param, 0, param.length);
                ostream.flush();
                ostream.close();
            }
            connection.connect();


            if(connection.getResponseCode()==200) {
                InputStream inputStream = connection.getInputStream();
                if(inputStream != null){
                    byte[]buffer = new byte[1024];
                    int len;
                    stream = new ByteArrayOutputStream();
                    while((len=inputStream.read(buffer))!=-1){
                        buffer.toString();
                        stream.write(buffer,0,len);
                    }
                }
            }
            else {
                stream = null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return stream;
    }


}
