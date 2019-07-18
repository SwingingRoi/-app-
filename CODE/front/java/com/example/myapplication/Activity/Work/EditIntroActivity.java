package com.example.myapplication.Activity.Work;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.myapplication.R;
import com.example.myapplication.MyComponent.MyToast;

public class EditIntroActivity extends AppCompatActivity {

    private String intro;
    private EditText Intro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_intro);

        Intro = findViewById(R.id.EditIntro);
        Intent intent = getIntent();
        if(intent.getExtras()!=null){
            intro = intent.getExtras().getString("intro","");
        }
        Intro.setText(intro);
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

    public void storeIntro(View view){
        String intro = Intro.getText().toString();

        if(intro.length()>200){
            new MyToast(this,getResources().getString(R.string.introlong));
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("intro",intro);
        this.setResult(RESULT_OK,intent);
        this.finish();
    }
}
