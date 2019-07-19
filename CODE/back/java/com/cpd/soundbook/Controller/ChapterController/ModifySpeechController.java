package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class ModifySpeechController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/updateSpeech")
    public void updateSpeech(HttpServletRequest request,
                             @RequestParam("oldpath") String oldpath, HttpServletResponse response){
        try{
            File newspeech = httpUtils.getMp3Param(request);
            httpUtils.writeStringBack(response,chapterService.updateSpeech(oldpath, newspeech));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
