package com.example.myapplication;

import android.content.Context;

import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

public class MySpinner extends AppCompatSpinner{
    public MySpinner(Context context){
        super(context);
    }

    public MySpinner(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
    }

    public MySpinner(Context context,AttributeSet attributeSet,int style){
        super(context,attributeSet,style);
    }

    //重写使得选项不变时仍能触发select事件
    @Override
    public void setSelection(int position){
        boolean isSame = position == getSelectedItemPosition();
        super.setSelection(position);
        if(isSame){
            getOnItemSelectedListener().onItemSelected(this,getSelectedView(),position,getSelectedItemId());
        }
    }
}
