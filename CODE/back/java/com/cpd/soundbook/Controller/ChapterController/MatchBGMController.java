package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.ChapterService;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@RestController
public class MatchBGMController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/matchBGM")
    public void matchBGM(HttpServletRequest request, HttpServletResponse response){
        try{
            //System.out.println("match bgm begin");
            JSONObject param = new JSONObject(httpUtils.getStringParam(request));
            String bgm = chapterService.matchBGM(param.getString("text"));
            JSONObject path = new JSONObject();
            path.put("bgmPath",bgm);
            httpUtils.writeStringBack(response,path.toString());
            //System.out.println("match bgm done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
