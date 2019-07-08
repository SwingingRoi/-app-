package com.example.myapplication;

import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import java.io.File;

public class GetAudioLength {
    public String getLength(File MP3file){
        String result = "";

        try{
            MP3File file = new MP3File(MP3file.getAbsolutePath());
            MP3AudioHeader audioHeader = (MP3AudioHeader) file.getAudioHeader();
            result = audioHeader.getTrackLengthAsString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
