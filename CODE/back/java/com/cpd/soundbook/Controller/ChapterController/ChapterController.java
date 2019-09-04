package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ChapterController {

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

    @RequestMapping("/audiobook/modifychapter")
    public void modifyChapter(HttpServletRequest request){
        try{
            JSONObject chapter = new JSONObject(httpUtils.getStringParam(request));
            System.out.println(chapter.toString());
            chapterService.modifyChapter(chapter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/deletechapters")
    public void deleteChapters(HttpServletRequest request){
        try{
            JSONObject ids = new JSONObject(httpUtils.getStringParam(request));
            chapterService.deleteChapters(ids);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
