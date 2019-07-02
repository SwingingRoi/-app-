package com.cpd.soundbook.Controller.ChapterController;


import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class GetChaptersController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getChapters")
    public void getChapters(@RequestParam("bookid") int bookid, @RequestParam("from") int from,
                            @RequestParam("size") int size, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response, chapterService.getChapters(bookid, from, size).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
