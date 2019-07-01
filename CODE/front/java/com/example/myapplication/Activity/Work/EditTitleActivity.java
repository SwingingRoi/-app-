package com.example.myapplication.Activity.Work;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.myapplication.R;
import com.example.myapplication.MyToast;

public class EditTitleActivity extends AppCompatActivity {

    private String title;
    private EditText Title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_title);

        Title = findViewById(R.id.EditTitle);
        Intent intent = getIntent();
        if(intent.getExtras()!=null) {
            title = intent.getExtras().getString("title", "");
        }
        Title.setText(title);
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

    public void clear(View view){
        Title.setText("");
    }

    public void storeTitle(View view){
        String title = Title.getText().toString();

        if(title.length()==0){
            new MyToast(this,getResources().getString(R.string.titlenull));
            return;
        }

        if(title.length()>20){
            new MyToast(this,getResources().getString(R.string.titlelong));
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("title",title);
        this.setResult(RESULT_OK,intent);
        this.finish();
    }
}
