package com.cpd.soundbook.Controller.BrowseController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.UserBrowseBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class SearchRecordController {

    @Autowired
    private UserBrowseBookService userBrowseBookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/searchHistory")
    public void searchRecord(@RequestParam("account") String account, @RequestParam("day") int day,
                             @RequestParam("from") int from, @RequestParam("size") int size, HttpServletResponse response)
    {
        try{
            httpUtils.writeStringBack(response,userBrowseBookService.searchRecords(URLDecoder.decode(account, "UTF-8"), day, from, size).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
