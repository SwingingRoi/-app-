package com.cpd.soundbook.Controller.DraftController;

import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.DraftService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class StoreDraftController {

    @Autowired
    private DraftService draftService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/storeDraft")
    public void storeDraft(HttpServletRequest request)
    {
        try{
            JSONObject info = new JSONObject(httpUtils.getStringParam(request));
            draftService.storeDraft(info.getInt("bookid"),info.getString("draft"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
