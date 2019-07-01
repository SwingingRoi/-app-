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
public class UpdateUserController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/updateuser")
    public void update(HttpServletRequest request, HttpServletResponse response){
        try {
            String paramStr = httpUtils.getStringParam(request);
            JSONObject param = new JSONObject(paramStr);
            httpUtils.writeStringBack(response,userService.updateUser(param));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
