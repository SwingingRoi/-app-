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
public class SignController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/sign")
    public void sign(HttpServletRequest request, HttpServletResponse response){
        try {
            JSONObject newUser = new JSONObject(httpUtils.getStringParam(request));
            String result = userService.sign(newUser);
            httpUtils.writeStringBack(response,result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
