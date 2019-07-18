package com.cpd.soundbook.Service.ServiceImpl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class ChapterServiceTest {
    @Autowired
    private ChapterService chapterService;

    @Test
    public void storeChapter() {
        try {
            //测试保存章节
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bookid", 4);
            jsonObject.put("title","test");
            jsonObject.put("content","测试用内容");
            jsonObject.put("speechPath","测试音频路径");
            jsonObject.put("length","00:10");
            jsonObject.put("bgmPath","测试用背景音路径");
            chapterService.storeChapter(jsonObject);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getChapters() {
        try{
            //测试书本的章节获取
            JSONArray result=new JSONArray();
            JSONObject object1=new JSONObject();
            object1.put("id",2);
            object1.put("bookid",1);
            object1.put("title","书1章节1");
            object1.put("length","00:10");
            result.put(object1);
            JSONObject object2=new JSONObject();
            object2.put("id",3);
            object2.put("bookid",1);
            object2.put("title","书1章节2");
            object2.put("length","00:10");
            result.put(object2);
            Assert.assertEquals(result.toString(),chapterService.getChapters(1,0,2).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void deleteChapters() {
        try {
            //测试章节删除
            JSONObject deleteids=new JSONObject();
            JSONArray ids=new JSONArray();
            JSONObject id=new JSONObject();
            id.put("id",4);
            deleteids.put("bookid",4);
            deleteids.put("ids",ids);
            chapterService.deleteChapters(deleteids);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getChapterByID() {
        try{
            //测试章节具体内容的获取
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("id",2);
            jsonObject.put("title","书1章节1");
            jsonObject.put("content","test1");
            jsonObject.put("speechPath","spath1");
            jsonObject.put("bgmPath","bpath1");
            jsonObject.put("length","00:10");
            Assert.assertEquals(jsonObject.toString(),chapterService.getChapterByID(2).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void modifyChapter() {
        try {
            JSONObject modify=new JSONObject();
            modify.put("id",3);
            modify.put("title","书1章节2");
            modify.put("content","modify");
            modify.put("speechPath","spath2");
            modify.put("bgmPath","bpath2");
            modify.put("length","00:10");
            chapterService.modifyChapter(modify);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getBGM(){
        try {
            String filename="bpath2.mp3";
            Assert.assertEquals(filename,chapterService.getBGM("bpath2.mp3").getFilename());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getSpeech() {
        try {
            String filename="spath2.mp3";
            Assert.assertEquals(filename,chapterService.getBGM("spath2.mp3").getFilename());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void updateSpeech() {
        try {
            File file1 = new File(System.getProperty("user.dir")+"\\"+System.currentTimeMillis()+".mp3");
            chapterService.updateSpeech("spath2",file1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}