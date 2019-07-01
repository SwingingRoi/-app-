package com.cpd.soundbook.Controller.UserController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class LogController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/log")
    public void log(HttpServletRequest request, HttpServletResponse response){
        try{
            JSONObject user = new JSONObject(httpUtils.getStringParam(request));
            String result = userService.log(user);
            httpUtils.writeStringBack(response,result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
