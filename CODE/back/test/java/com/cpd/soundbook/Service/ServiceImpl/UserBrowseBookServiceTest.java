package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Entity.UserBrowseBook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class UserBrowseBookServiceTest {
    @Autowired
    private UserBrowseBookService userBrowseBookService;

    @Test
    public void addRecord() {
        try {
            //增加浏览历史测试
            JSONObject record = new JSONObject();
            record.put("account", "test1");
            record.put("id",1);
            record.put("time","1560000000000");
            userBrowseBookService.addRecord(record);
            record.put("account", "test1");
            record.put("id",2);
            record.put("time","1560000000000");
            userBrowseBookService.addRecord(record);
            record.put("account", "test2");
            record.put("id",1);
            record.put("time","1560000000000");
            userBrowseBookService.addRecord(record);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getRecords() {
        try{
            //获取用户浏览过的书
            JSONArray books=new JSONArray();
            JSONObject object1 = new JSONObject();
            object1.put("name","天体观测");
            object1.put("id",3);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            long timStamp = Long.parseLong("1560000000000");
            Date date = new Date(timStamp);
            object1.put("time",simpleDateFormat.format(date));
            object1.put("bookid",1);
            object1.put("tags","科学世界");
            books.put(object1);

            JSONObject object2 = new JSONObject();
            object2.put("name","你的赠予");
            object2.put("id",4);
            object2.put("time",simpleDateFormat.format(date));
            object2.put("bookid",2);
            object2.put("tags","言情");
            books.put(object2);

            Assert.assertEquals(books.toString(),userBrowseBookService.getRecords("vagrant",0,2).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void searchRecords() {
        //查找指定的浏览历史
        try {
            JSONArray books=new JSONArray();
            JSONObject object1 = new JSONObject();
            object1.put("name","天体观测");
            object1.put("id",3);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            long timStamp = Long.parseLong("1560000000000");
            Date date = new Date(timStamp);
            object1.put("time",simpleDateFormat.format(date));
            object1.put("bookid",1);
            object1.put("tags","科学世界");
            books.put(object1);

            Assert.assertEquals(books.toString(),userBrowseBookService.searchRecords("vagrant",3,0,1).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void deleteRecords() {
        try {
            //删除某条浏览历史测试
            JSONArray ids = new JSONArray();
            JSONObject id = new JSONObject();
            id.put("id", 13);
            ids.put(id);
            userBrowseBookService.deleteRecords(ids);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void clearRecords() {
        userBrowseBookService.clearRecords("test1");
    }
}