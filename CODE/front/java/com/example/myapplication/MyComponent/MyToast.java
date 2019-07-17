package com.example.myapplication.MyComponent;

import android.content.Context;
import android.widget.Toast;

public class MyToast {

    private final int time = Toast.LENGTH_SHORT;
    public MyToast(Context context,String msg){
        Toast toast = Toast.makeText(context,msg,time);
        toast.show();
    }
}
