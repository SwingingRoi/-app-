package com.cpd.soundbook.Controller.UserController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class SaveAvatarController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/saveAvatar")
    public void saveAvatar(HttpServletRequest request, HttpServletResponse response){
        File avatar = httpUtils.getPngParam(request);
        httpUtils.writeStringBack(response,userService.saveAvatar(avatar));
    }
}
