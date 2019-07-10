package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class TextToSpeechController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/textToSpeech")
    public void textToSpeech(HttpServletRequest request, HttpServletResponse response){
        try{
            JSONObject text = new JSONObject(httpUtils.getStringParam(request));
            File result = chapterService.textToSpeech(text.getString("text"));

            httpUtils.writeFileBack(response,result);
            //result.delete();//删除临时文件
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
