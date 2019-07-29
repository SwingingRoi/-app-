package com.example.myapplication.Activity.Account;

import android.app.Fragment;
import android.content.Intent;

import com.example.myapplication.Activity.LogSign.LogActivity;
import com.example.myapplication.Activity.Setting.SettingActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.myapplication.PicUtils.GetPicture;
import com.example.myapplication.Activity.Work.PersonalWorkActivity;
import com.example.myapplication.Activity.History.HistoryActivity;
import com.example.myapplication.Activity.Favorite.FavoriteActivity;

import com.example.myapplication.R;

import static android.content.Context.MODE_PRIVATE;

public class PersonalFragment extends Fragment {
    private String account;
    private boolean hasLogged;
    private ImageView Avatar;
    private TextView Account;
    private boolean isInNight = false;//是否处于夜间模式


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserState",MODE_PRIVATE);
        hasLogged = sharedPreferences.getBoolean("HasLogged",false);
        account = sharedPreferences.getString("account",getResources().getString(R.string.goLog));
        isInNight = sharedPreferences.getBoolean("night",false);//是否处于夜间模式

        View view;
        if(isInNight){
            view = inflater.inflate(R.layout.activity_personal_night, container, false);
        }else {
            view = inflater.inflate(R.layout.activity_personal, container, false);
        }

        Avatar = view.findViewById(R.id.Avatar);
        Account = view.findViewById(R.id.Account);
        ImageView Setting = view.findViewById(R.id.Setting);

        new Thread(getAvatar).start();

        Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),SettingActivity.class);
                startActivity(intent);
            }
        });


        LinearLayout editLayout = view.findViewById(R.id.EditLayout);
        LinearLayout workLayout = view.findViewById(R.id.WorkLayout);
        LinearLayout historyLayout = view.findViewById(R.id.HistoryLayout);
        LinearLayout favoriteLayout = view.findViewById(R.id.FavoriteLayout);

        if(hasLogged) {//已登录,点击头像或文本跳转至个人信息界面
            Account.setText(account);
            Account.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),EditInfoActivity.class);
                    startActivity(intent);
                }
            });
            Avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                    startActivity(intent);
                }
            });
            editLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                    startActivity(intent);
                }
            });
            workLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),PersonalWorkActivity.class);
                    startActivity(intent);
                }
            });
            historyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),HistoryActivity.class);
                    startActivity(intent);
                }
            });
            favoriteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),FavoriteActivity.class);
                    startActivity(intent);
                }
            });
        }

        else {//未登录，点击头像或文本跳转至登录界面
            Account.setText(getResources().getString(R.string.goLog));
            Avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LogActivity.class);
                    startActivity(intent);
                }
            });

            Account.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LogActivity.class);
                    startActivity(intent);
                }
            });
            editLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LogActivity.class);
                    startActivity(intent);
                }
            });
            workLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),LogActivity.class);
                    startActivity(intent);
                }
            });
            historyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),LogActivity.class);
                    startActivity(intent);
                }
            });
            favoriteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),LogActivity.class);
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    //获取头像
    Runnable getAvatar = new Runnable() {
        @Override
        public void run() {
            GetPicture getPicture = new GetPicture();
            final Bitmap avatar = getPicture.getAvatar(account);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (avatar != null) {
                        Avatar.setImageBitmap(avatar);
                    }
                }
            });
        }
    };

    //从个人中心返回，检查用户名、头像是否被修改
    @Override
    public void onResume() {
        super.onResume();


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserState",MODE_PRIVATE);

        boolean isNight = sharedPreferences.getBoolean("night",false);
        if(isNight != isInNight){
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
            return;
        }
        account = sharedPreferences.getString("Account","");
        if(account.equals("")) return;
        Account.setText(account);
        new Thread(getAvatar).start();
    }
}
