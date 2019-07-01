package com.example.myapplication.PicUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class DrawAsCircle {
    public Bitmap draw(Bitmap origin){
        Bitmap output=null;
        try{
            output = Bitmap.createBitmap( origin.getWidth(), origin.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);


            final Paint paint = new Paint();
            final Rect rect = new Rect( 0,0,origin.getWidth(),origin.getHeight());
            paint.setAntiAlias(true);//抗锯齿
            paint.setFilterBitmap(true);//抗锯齿
            paint.setDither(true);//防抖动
            canvas.drawARGB(0,0,0,0);

            //在画布上绘制一个圆
            canvas.drawCircle(origin.getWidth() / (float)2,origin.getHeight() / (float)2,origin.getWidth() / (float)2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(origin,rect,rect,paint);
        }catch (Exception e){
            e.printStackTrace();
        }
        return output;
    }
}
