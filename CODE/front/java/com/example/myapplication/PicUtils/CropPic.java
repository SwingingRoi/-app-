package com.example.myapplication.PicUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;


public class CropPic {

    static final String IMAGE_LOCATION = Environment.getExternalStorageDirectory().getPath()+"/temp.png";

    public Uri setCropParam(Intent intent, Uri uri){
        //将图片存在本地文件夹，减少模糊
        File file = new File(IMAGE_LOCATION);
        Uri imageUri = Uri.fromFile(file);
        intent.setAction("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        return imageUri;
    }
}
