package com.cpd.soundbook.Controller.SpeechController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class TextToSpeechController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/textToSpeech")
    public void textToSpeech(HttpServletRequest request, HttpServletResponse response){
        try{
            //System.out.println("text to speech begin");
            JSONObject text = new JSONObject(httpUtils.getStringParam(request));
            httpUtils.writeStringBack(response,chapterService.textToSpeech(text.getString("text")));
            //System.out.println("text to speech done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
