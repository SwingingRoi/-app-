package com.example.myapplication.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.Activity.Account.PersonalFragment;
import com.example.myapplication.Activity.Home.HomeFragment;
import com.example.myapplication.MyComponent.MyToast;
import com.example.myapplication.R;
import com.example.myapplication.Activity.Recommend.RecommendFragment;

public class MainActivity extends AppCompatActivity{

    public HomeFragment homeFragment;
    public RecommendFragment recommendFragment;
    public PersonalFragment personalFragment;//两部分,首页、个人中心、制作
    private FragmentManager manager;
    private boolean hasBacked=false;
    private boolean isInNight = false;//是否处于夜间模式
    private final Long TIME_DISABLED=Long.parseLong("86400000");//设置若一天没有进入APP，需重新登录
    private final int HOME=0;
    private final int RECOMMEND = 1;
    private final int PERSONAL = 2;
    private int WHICH=HOME;//标志当前选中哪个fragment,初始为HOME
    private Fragment[]fragments;

    final private String NIGHT_TEXT_COLOR = "#888282";//夜间模式字体颜色
    final private String PRESSED_TEXT_COLOR = "#f76442";//被选择后的字体颜色

    @Override
    public void onBackPressed(){
        if(hasBacked){
            Intent backHome = new Intent(Intent.ACTION_MAIN);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(backHome);
            hasBacked = false;
            return;
        }
        new MyToast(this,"再按一次返回桌面");
        hasBacked = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("android:support:fragments",null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //判断是否登录
        SharedPreferences sharedPreferences = getSharedPreferences("UserState",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Long timeNow = System.currentTimeMillis();//当前时间
        Long timeLast = sharedPreferences.getLong("time",timeNow);//上一次使用的时间
        editor.putLong("time",timeNow);//更新preference中的时间
        editor.apply();

        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        super.onCreate(savedInstanceState);
        if(isInNight){
            setContentView(R.layout.activity_main_night);
        }else {
            setContentView(R.layout.activity_main);
        }

        manager = getFragmentManager();
        homeFragment = new HomeFragment();
        recommendFragment = new RecommendFragment();
        personalFragment = new PersonalFragment();
        fragments = new Fragment[]{homeFragment,recommendFragment,personalFragment};

        if(timeNow - timeLast > TIME_DISABLED){
            editor.putString("Account","");
            editor.putBoolean("HasLogged",false);
            editor.apply();
        }//如果超过一天未使用APP，清除登录信息

        boolean hasLogged = sharedPreferences.getBoolean("HasLogged",false);
        if(hasLogged){
            TextView personalText = findViewById(R.id.PersonalText);
            personalText.setText(R.string.Personal);
        }

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.content,homeFragment);
        transaction.show(homeFragment);
        transaction.commit();
        setStyle();
    }


    //底部导航栏样式设置
    private void setHome(){
        TextView homeText = findViewById(R.id.HomeText);
        ImageView homeIcon = findViewById(R.id.HomeIcon);
        if(WHICH==HOME) {
            homeText.setTextColor(Color.parseColor(PRESSED_TEXT_COLOR));
            homeIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.homeiconpressed));
        }
        else {
            if(isInNight){
                homeText.setTextColor(Color.parseColor(NIGHT_TEXT_COLOR));
                homeIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.homeiconblue));
            }else {
                homeText.setTextColor(Color.GRAY);
                homeIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.homeicon));
            }
        }
    }

    private void setRecommend(){
        TextView recommendText = findViewById(R.id.RecommendText);
        ImageView recommendIcon = findViewById(R.id.RecommendIcon);
        if(WHICH == RECOMMEND){
            recommendText.setTextColor(Color.parseColor(PRESSED_TEXT_COLOR));
            recommendIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.recommendpressed));
        }
        else {
            if(isInNight){
                recommendText.setTextColor(Color.parseColor(NIGHT_TEXT_COLOR));
                recommendIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.recommendblue));
            }else {
                recommendText.setTextColor(Color.GRAY);
                recommendIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.recommend));
            }
        }
    }


    private void setPersonal(){
        TextView personalText = findViewById(R.id.PersonalText);
        ImageView personalIcon = findViewById(R.id.PersonalIcon);
        if(WHICH==PERSONAL){
            personalText.setTextColor(Color.parseColor(PRESSED_TEXT_COLOR));
            personalIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.personaliconpressed));
        }
        else {
            if(isInNight){
                personalText.setTextColor(Color.parseColor(NIGHT_TEXT_COLOR));
                personalIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.personaliconblue));
            }else {
                personalText.setTextColor(Color.GRAY);
                personalIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.personalicon));
            }
        }
    }

    private void setStyle(){
        setHome();
        setRecommend();
        setPersonal();
    }

    //不同fragment之间的跳转
    public void toHome(View view){
        if(WHICH==HOME) return;
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.hide(fragments[WHICH]);
        if (!fragments[HOME].isAdded()){
            transaction.add(R.id.content,fragments[HOME]);
        }
        hasBacked = false;
        transaction.show(fragments[HOME]);
        transaction.commit();

        WHICH=HOME;
        setStyle();
    }

    public void toRecommend(View view){
        if(WHICH ==  RECOMMEND) return;
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.hide(fragments[WHICH]);
        if(!fragments[RECOMMEND].isAdded()){
            transaction.add(R.id.content,fragments[RECOMMEND]);
        }
        hasBacked = false;
        transaction.show(fragments[RECOMMEND]);
        transaction.commit();

        WHICH = RECOMMEND;
        setStyle();
    }

    public void toPersonal(View view){
        if(WHICH == PERSONAL) return;
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.hide(fragments[WHICH]);
        if(!fragments[PERSONAL].isAdded()){
            transaction.add(R.id.content,fragments[PERSONAL]);
        }
        hasBacked = false;
        transaction.show(fragments[PERSONAL]);
        transaction.commit();

        WHICH=PERSONAL;
        setStyle();
    }

}
