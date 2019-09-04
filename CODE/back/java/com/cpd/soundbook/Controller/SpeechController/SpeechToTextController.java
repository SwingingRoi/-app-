package com.cpd.soundbook.Controller.SpeechController;

import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.ChapterService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class SpeechToTextController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/speechToText")
    public void speechToText(HttpServletRequest request, HttpServletResponse response){
        try{
            //System.out.println("speech to text begin");
            File srcFile = httpUtils.getMp3Param(request);
            //File srcFile = new File("C:\\Users\\09\\Desktop\\test\\15670882725344.mp3");
            //System.out.println("srcFile:" + srcFile.getAbsolutePath());
            //File srcFile = new File("D:\\AudiobookBack\\mp3\\result\\Vq7ZqMNUIbR5jC3.mp3");
            JSONObject result = chapterService.speechToText(srcFile);
            //System.out.println(result);
            if(result == null) {
                httpUtils.writeStringBack(response,"error");
                if(srcFile.exists()) srcFile.delete();
                return;
            }
            httpUtils.writeStringBack(response,result.toString());
            if(srcFile.exists()) srcFile.delete();
            //System.out.println("speech to text done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
