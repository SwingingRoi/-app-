package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@RestController
public class SpeechToTextController {

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/SpeechToText")
    public void speechToText(HttpServletRequest request){
        try{

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
