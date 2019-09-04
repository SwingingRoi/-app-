package com.cpd.soundbook.Controller.SpeechController;

import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class SpeechController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/storeSpeech")
    public void storeSpeech(HttpServletRequest request, HttpServletResponse response){
        try{
            File file = httpUtils.getMp3Param(request);
            httpUtils.writeStringBack(response,chapterService.storeSpeech(file));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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

    @RequestMapping("/audiobook/deleteSpeech")
    public void deleteSpeech(@RequestParam("path") String path){
        try{
            chapterService.deletSpeech(path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
