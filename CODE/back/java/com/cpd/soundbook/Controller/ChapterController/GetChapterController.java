package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class GetChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getchapterbyid")
    public void getChapter(@RequestParam("id") int id, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,chapterService.getChapterByID(id).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
