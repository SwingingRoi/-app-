package com.cpd.soundbook.Controller.DraftController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.DraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class GetDraftController {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private DraftService draftService;

    @RequestMapping("audiobook/getDraft")
    public void getDraft(@RequestParam("bookid") int bookid, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,draftService.getDraft(bookid));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
