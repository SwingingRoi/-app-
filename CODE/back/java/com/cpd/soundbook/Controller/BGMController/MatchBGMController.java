package com.cpd.soundbook.Controller.BGMController;

import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
public class MatchBGMController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/matchBGMByText")
    public void matchBGMByText(HttpServletRequest request, HttpServletResponse response){
        try{
            //System.out.println("match bgm begin");
            JSONObject param = new JSONObject(httpUtils.getStringParam(request));


            String bgm = chapterService.matchBGMByText(param.getString("text"));
            JSONObject path = new JSONObject();
            path.put("bgmPath",bgm);
            httpUtils.writeStringBack(response,path.toString());
            //System.out.println("match bgm done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*@RequestMapping("/audiobook/matchBGMByAudio")
    public void matchBGMByAudio(HttpServletRequest request,HttpServletResponse response){
        try{
            File srcFile = new File("C:\\Users\\09\\Desktop\\test\\wenyiduo.mp3");
            //File srcFile = httpUtils.getMp3Param(request);
            String bgm = chapterService.matchBGMByAudio(srcFile);
            System.out.println(bgm);
            JSONObject path = new JSONObject();
            path.put("bgmPath",bgm);
            httpUtils.writeStringBack(response,path.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/
}
