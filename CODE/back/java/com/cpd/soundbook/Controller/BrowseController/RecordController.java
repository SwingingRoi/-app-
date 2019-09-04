package com.cpd.soundbook.Controller.BrowseController;

import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserBrowseBookService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

@RestController
public class RecordController {

    @Autowired
    private UserBrowseBookService userBrowseBookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/addrecord")
    public void addRecord(HttpServletRequest request){
        try{
            JSONObject record = new JSONObject(httpUtils.getStringParam(request));
            userBrowseBookService.addRecord(record);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/deleteHistory")
    public void deleteRecord(HttpServletRequest request){
        try{
            JSONArray ids = new JSONArray(httpUtils.getStringParam(request));
            userBrowseBookService.deleteRecords(ids);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/clearHistory")
    public void clearHistory(@RequestParam("account") String account){
        try{
            userBrowseBookService.clearRecords(URLDecoder.decode(account, "UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
