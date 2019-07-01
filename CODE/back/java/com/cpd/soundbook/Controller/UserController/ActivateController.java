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
public class ActivateController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/activate")
    public void activate(@RequestParam("account") String account, @RequestParam("code") String code, HttpServletResponse response){
        try {
            httpUtils.writeStringBack(response,userService.activate(URLDecoder.decode(account, "UTF-8")));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
