package com.cpd.soundbook.Controller.BrowseController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserBrowseBookService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class AddRecordController {

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
}
