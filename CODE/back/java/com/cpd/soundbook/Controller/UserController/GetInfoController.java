package com.cpd.soundbook.Controller.UserController;

import com.cpd.soundbook.Entity.User;
import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class GetInfoController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/info")
    public void getInfo(@RequestParam("account") String account, HttpServletResponse response){
        try{
            JSONObject info = userService.getInfo(URLDecoder.decode(account, "UTF-8"));
            if(info!=null){
                httpUtils.writeStringBack(response,info.toString());
            }
            else {
                httpUtils.writeStringBack(response, null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
