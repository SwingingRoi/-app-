package com.cpd.soundbook.Controller.SpeechController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.ChapterService;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@RestController
public class GetSpeechController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getSpeech")
    public void getSpeech(@RequestParam("path") String path, HttpServletResponse response){
        try{
            GridFSDBFile speech = chapterService.getSpeech(path);
            if(speech==null){
                httpUtils.writeStringBack(response,null);
            }
            else {
                OutputStream outputStream = response.getOutputStream();
                speech.writeTo(outputStream);
                outputStream.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
