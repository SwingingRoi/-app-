package com.cpd.soundbook.Controller.UserController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.UserService;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;

@RestController
public class AvatarController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/saveAvatar")
    public void saveAvatar(HttpServletRequest request, HttpServletResponse response){
        File avatar = httpUtils.getPngParam(request);
        httpUtils.writeStringBack(response,userService.saveAvatar(avatar));
    }

    @RequestMapping("/audiobook/saveAvatarName")
    public void saveAvatarName(@RequestParam("id") int id, @RequestParam("avatar") String avatar){
        try {
            JSONObject param = new JSONObject();
            param.put("id",id);
            param.put("avatar",avatar);
            userService.saveAvatarName(param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/getAvatar")
    public void getAvatar(@RequestParam("account") String account, HttpServletResponse response){
        try {
            GridFSDBFile avatar = userService.getAvatar(account);
            if(avatar==null){
                httpUtils.writeStringBack(response,null);
            }
            else {
                OutputStream outputStream = response.getOutputStream();
                response.setContentType("image/*");
                avatar.writeTo(outputStream);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
