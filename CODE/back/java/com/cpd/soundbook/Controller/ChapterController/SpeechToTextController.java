package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class SpeechToTextController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/speechToText")
    public void speechToText(HttpServletRequest request, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,
                    chapterService.speechToText(httpUtils.getMp3Param(request)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
