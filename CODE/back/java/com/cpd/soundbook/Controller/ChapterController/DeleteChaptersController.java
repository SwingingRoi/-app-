package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DeleteChaptersController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

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
