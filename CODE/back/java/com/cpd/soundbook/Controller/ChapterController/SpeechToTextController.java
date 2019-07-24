package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class SpeechToTextController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/speechToText")
    public void speechToText(HttpServletRequest request, HttpServletResponse response){
        try{
            File result = chapterService.speechToText(httpUtils.getMp3Param(request));
            httpUtils.writeFileBack(response,result);
            if(result != null) result.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
