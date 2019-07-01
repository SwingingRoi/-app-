package com.cpd.soundbook.Controller.UserController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaveAvatarNameController {
    @Autowired
    private UserService userService;

    @RequestMapping("/audiobook/saveAvatarName")
    public void saveAvatarName(@RequestParam("id") int id,@RequestParam("avatar") String avatar){
        try {
            JSONObject param = new JSONObject();
            param.put("id",id);
            param.put("avatar",avatar);
            userService.saveAvatarName(param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
