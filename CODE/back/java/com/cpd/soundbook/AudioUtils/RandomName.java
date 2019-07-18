package com.cpd.soundbook.AudioUtils;

import org.springframework.stereotype.Component;

import java.util.Random;

//生成随机文件名
@Component(value = "randomName")
public class RandomName {
    String choices = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String randomName (){
        String result = "";

        try{
            for(int i=0;i<15;i++){
                int random = new Random().nextInt(61);
                String s = choices.substring(random,random+1);
                result += s;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
