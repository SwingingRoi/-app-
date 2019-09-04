package com.cpd.soundbook.Controller.UserController;

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
public class UserController {

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

    @RequestMapping("/audiobook/activate")
    public void activate(@RequestParam("account") String account, @RequestParam("code") String code, HttpServletResponse response){
        try {
            httpUtils.writeStringBack(response,userService.activate(URLDecoder.decode(account, "UTF-8")));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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

    @RequestMapping("/audiobook/getrecommend")
    public void getRecommend(@RequestParam("account") String account, @RequestParam("from") int from, @RequestParam("size") int size,
                             HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,userService.getRecommend(URLDecoder.decode(account,"UTF-8"), from, size).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/getPreference")
    public void getPreference(@RequestParam("account") String account, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,userService.getPreference(URLDecoder.decode(account,"UTF-8")));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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
