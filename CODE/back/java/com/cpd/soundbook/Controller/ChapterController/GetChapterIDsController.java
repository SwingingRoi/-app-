package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class GetChapterIDsController {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private ChapterService chapterService;

    @RequestMapping("/audiobook/getchapterIDs")
    public void getChapterIDs(@RequestParam("bookid") int bookid, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,chapterService.getChapterIDs(bookid).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
