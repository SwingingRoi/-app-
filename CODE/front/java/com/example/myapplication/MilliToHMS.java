package com.example.myapplication;

public class MilliToHMS {
    public String milliToHMS(int milliSeconds){
        String result = "";
        try{
            int hour = milliSeconds / 3600000;
            int minute = (milliSeconds % 3600000) / 60000;
            int second = (milliSeconds % 60000) / 1000;

            if(hour > 0 && hour < 10) result += "0" + hour;
            if(hour >=10) result += hour;

            if(hour > 0) result += ":";

            if(minute <10) result += "0" + minute;
            else result += minute;

            result += ":";

            if(second <10) result += "0" + second;
            else  result += second;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
