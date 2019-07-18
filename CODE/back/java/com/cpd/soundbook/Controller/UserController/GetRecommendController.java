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
public class GetRecommendController {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private UserService userService;

    @RequestMapping("/audiobook/getrecommend")
    public void getRecommend(@RequestParam("account") String account, @RequestParam("from") int from, @RequestParam("size") int size,
                             HttpServletResponse response){
        try{
            System.out.println(1);
            httpUtils.writeStringBack(response,userService.getRecommend(URLDecoder.decode(account,"UTF-8"), from, size).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
