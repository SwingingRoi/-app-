package com.cpd.soundbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class GetBGMController {

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getBGM")
    public void getBGM(HttpServletResponse response){
        try{
            File bgm = new File("D:\\CloudMusic\\DAOKO,米津玄師 - 打上花火.mp3");
            httpUtils.writeFileBack(response,bgm);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
