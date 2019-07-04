package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@RestController
public class StoreSpeechController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/storeSpeech")
    public void storeSpeech(HttpServletRequest request, @RequestParam("path") String path){
        try{
            File file = httpUtils.getMp3Param(request,path);
            chapterService.storeSpeech(file);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
