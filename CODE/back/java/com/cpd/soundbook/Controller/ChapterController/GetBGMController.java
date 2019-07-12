package com.cpd.soundbook.Controller.ChapterController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.ChapterService;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.net.URLDecoder;

@RestController
public class GetBGMController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getBGM")
    public void getBGM(@RequestParam("filename") String filename, HttpServletResponse response){
        try{
            String fileName = URLDecoder.decode(filename,"UTF-8");
            GridFSDBFile bgm = chapterService.getBGM(fileName);
            if(bgm==null){
                httpUtils.writeStringBack(response,null);
            }
            else {
                OutputStream outputStream = response.getOutputStream();
                bgm.writeTo(outputStream);
                outputStream.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
