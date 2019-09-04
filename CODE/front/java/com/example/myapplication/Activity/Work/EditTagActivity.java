package com.example.myapplication.Activity.Work;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class EditTagActivity extends AppCompatActivity {

    private List<EditText> tagViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        boolean isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight) setContentView(R.layout.activity_edit_tag_night);
        else setContentView(R.layout.activity_edit_tag);

        tagViews = new ArrayList<>();

        EditText tag1 = findViewById(R.id.tag1);
        tagViews.add(tag1);

        EditText tag2 = findViewById(R.id.tag2);
        tagViews.add(tag2);

        EditText tag3 = findViewById(R.id.tag3);
        tagViews.add(tag3);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            String tags = intent.getExtras().getString("tags","");
            String[] ts = tags.split(" ");
            for(int i=0;i<ts.length;i++){
                tagViews.get(i).setText(ts[i]);
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        this.setResult(RESULT_CANCELED,intent);
        this.finish();
    }

    public void onBackPressed(View view){
        onBackPressed();
    }

    public void storeTag(View view){
        String tags = "";
        for(EditText editText : tagViews){
            tags += editText.getText().toString() + " ";
        }

        Intent intent = new Intent();
        intent.putExtra("tags",tags);
        this.setResult(RESULT_OK,intent);
        this.finish();
    }
}
