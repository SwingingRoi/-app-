package com.cpd.soundbook.AudioUtils;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import com.baidu.aip.nlp.AipNlp;

/*
 * AipNlp是自然语言处理的Java客户端，为使用自然语言处理的开发人员提供了一系列的交互方法。
   用户可以参考如下代码新建一个AipNlp,初始化完成后建议单例使用,避免重复获取access_token：
 */

public class Sentiment {
    // 设置APPID/AK/SK
    private static final String APP_ID = "16743059";
    private static final String API_KEY = "hb1zr741j08cGDyy01P2TrGn";
    private static final String SECRET_KEY ="e1vtiIQnpHQtqrLOGT4kBbSSqRZQ7bnF";

    final private int INFINITE = 999999999;

    private static AipNlp instance = null;
    public static synchronized AipNlp getInstance() {
        if (instance == null) {
            instance = new AipNlp(APP_ID, API_KEY, SECRET_KEY);
        }

        return instance;
    }

    //等级计算
    private static int compute_level(double positive){
        int level = 0;
        double val=positive-0.5;
        val*=20;
        String num=String.valueOf(val);
        level=Integer.parseInt(num.substring(num.indexOf(".")+1,num.indexOf(".")+2));
        if(level>=5)//四舍五入
        {
            if(val>=0) level=Integer.parseInt(num.substring(0,num.indexOf(".")))+1;
            else level=Integer.parseInt(num.substring(0,num.indexOf(".")))-1;
        }
        else
        {
            level=Integer.parseInt(num.substring(0,num.indexOf(".")));
        }
        return level;
    }

    public int feelLevel(String text) {
        // 新建一个AipNlp,初始化完成后建议单例使用,避免重复获取access_token：
        // AipNlp client = new AipNlp(APP_ID, API_KEY, SECRET_KEY);
        AipNlp client = getInstance();
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(INFINITE);
        client.setSocketTimeoutInMillis(INFINITE);
        /*client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);*/

        double positive = 0.0;
        try {
            //文本参数
            // 传入可选参数调用接口
            HashMap<String, Object> options = new HashMap<String, Object>();
            // 情感倾向分析
            JSONObject res = client.sentimentClassify(text, options);

            JSONArray sentimentarray = res.getJSONArray("items");
            JSONObject sentimenttable = sentimentarray.getJSONObject(0);

            //取积极指数，为大于0小于1的小数
            positive = sentimenttable.getDouble("positive_prob");
        }catch (Exception e){
            //e.printStackTrace();
        }
        return compute_level(positive);
    }
}
