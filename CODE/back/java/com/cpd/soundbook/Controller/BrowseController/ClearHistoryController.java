package com.cpd.soundbook.Controller.BrowseController;


import com.cpd.soundbook.Service.ServiceInterface.UserBrowseBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;

@RestController
public class ClearHistoryController {

    @Autowired
    private UserBrowseBookService userBrowseBookService;

    @RequestMapping("/audiobook/clearHistory")
    public void clearHistory(@RequestParam("account") String account){
        try{
            userBrowseBookService.clearRecords(URLDecoder.decode(account, "UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
