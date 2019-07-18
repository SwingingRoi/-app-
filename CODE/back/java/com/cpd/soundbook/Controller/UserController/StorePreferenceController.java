package com.cpd.soundbook.Controller.UserController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

@RestController
public class StorePreferenceController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/storePreference")
    public void storePreference(HttpServletRequest request, @RequestParam("account") String account){
        try{
            JSONObject param = new JSONObject(httpUtils.getStringParam(request));
            userService.storePreference(URLDecoder.decode(account,"UTF-8"),
                    param.getString("tags"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
