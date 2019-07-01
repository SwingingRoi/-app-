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
public class GetRecordsController {

    @Autowired
    private UserBrowseBookService userBrowseBookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getHistory")
    public void getRecords(@RequestParam("account") String account, @RequestParam("from") int from,
                           @RequestParam("size") int size, HttpServletResponse response){
        try {
            httpUtils.writeStringBack(response, userBrowseBookService.getRecords(URLDecoder.decode(account, "UTF-8"), from, size).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
