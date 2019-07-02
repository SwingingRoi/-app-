package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class StoreChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/storeChapter")
    public void storeChapter(HttpServletRequest request){
        try{
            JSONObject chapter = new JSONObject(httpUtils.getStringParam(request));
            chapterService.storeChapter(chapter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
