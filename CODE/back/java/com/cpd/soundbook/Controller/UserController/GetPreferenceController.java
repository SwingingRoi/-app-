package com.cpd.soundbook.Controller.UserController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class GetPreferenceController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;


    @RequestMapping("/audiobook/getPreference")
    public void getPreference(@RequestParam("account") String account, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,userService.getPreference(URLDecoder.decode(account,"UTF-8")));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
