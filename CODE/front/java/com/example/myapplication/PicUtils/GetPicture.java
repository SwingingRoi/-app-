package com.example.myapplication.PicUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.myapplication.InternetUtils.GetServer;
import com.example.myapplication.InternetUtils.HttpUtils;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;


public class GetPicture {

    public Bitmap getAvatar(String account){
        Bitmap result = null;
        try {
            GetServer getServer = new GetServer();
            String url = getServer.getIPADDRESS() + "/audiobook/getAvatar?account=" + URLEncoder.encode(account,"UTF-8");

            HttpUtils httpUtils = new HttpUtils(url);
            ByteArrayOutputStream stream = httpUtils.doHttp(null, "GET", "image/*");
            if(stream!=null) {
                result = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.toByteArray().length);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap getSurface(int bookid){
        Bitmap result = null;
        try {
            GetServer getServer = new GetServer();
            String url = getServer.getIPADDRESS() + "/audiobook/getSurface?id=" + bookid;

            HttpUtils httpUtils = new HttpUtils(url);
            ByteArrayOutputStream stream = httpUtils.doHttp(null, "GET", "image/*");
            if(stream!=null) {
                result = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.toByteArray().length);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
