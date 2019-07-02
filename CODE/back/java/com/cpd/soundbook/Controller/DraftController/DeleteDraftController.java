package com.cpd.soundbook.Controller.DraftController;

import com.cpd.soundbook.Service.ServiceInterface.DraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteDraftController {

    @Autowired
    private DraftService draftService;

    @RequestMapping("/audiobook/deleteDraft")
    public void deleteDraft(@RequestParam("bookid") int bookid){
        try{
            draftService.deleteDraft(bookid);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
