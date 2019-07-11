package com.example.myapplication;

import android.media.MediaMetadataRetriever;


public class GetAudioLength {
    public int getLength(String fileName){
        int result = 0;

        try{
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(fileName);
            result = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            mediaMetadataRetriever.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
