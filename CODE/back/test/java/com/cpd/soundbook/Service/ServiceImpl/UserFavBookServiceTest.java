package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.Entity.Book;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class UserFavBookServiceTest {
    @Autowired
    private UserFavBookService userFavBookService;

    @Test
    public void addFav() {
        //正常添加进收藏夹
        Assert.assertEquals("success",userFavBookService.addFav("jhd8400",1));
    }

    @Test
    public void cancelFav() {
        try {
            //正确删除收藏的图书
            JSONArray ids1=new JSONArray();
            JSONObject id1=new JSONObject();
            JSONObject id2=new JSONObject();
            id1.put("id", 2);
            ids1.put(id1);
            id2.put("id",3);
            ids1.put(id2);
            Assert.assertEquals("success",userFavBookService.cancelFav("jhd8400",ids1));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void checkFav() {
        //用户收藏的书
        Assert.assertEquals("yes",userFavBookService.checkFav("vagrant",1));

        //用户未收藏的书
        Assert.assertEquals("no",userFavBookService.checkFav("vagrant",4));
    }

    @Test
    public void getFav() {
        //获取用户收藏的书
        JSONArray books=new JSONArray();
        Book book1=new Book();
        book1.setId(1);
        book1.setName("天体观测");
        book1.setIntro("寻觅繁星");
        book1.setAuthor("冰川日菜");
        book1.setSurface("1.jpg");
        book1.setChapters(5);
        book1.setViews(3);
        book1.setTags("科学世界");
        books.put(book1.toJSONObject());

        Book book2=new Book();
        book2.setId(2);
        book2.setName("你的赠予");
        book2.setIntro("里美的赠礼之歌");
        book2.setAuthor("牛込里美");
        book2.setSurface("2.jpg");
        book2.setChapters(4);
        book2.setViews(5);
        book2.setTags("言情");
        books.put(book2.toJSONObject());

        Assert.assertEquals(books.toString(),userFavBookService.getFav("vagrant",0,2).toString());
    }

    @Test
    public void searchFav() {
        //查找指定的收藏的书
        JSONArray books=new JSONArray();
        Book book1=new Book();
        book1.setId(1);
        book1.setName("天体观测");
        book1.setIntro("寻觅繁星");
        book1.setAuthor("冰川日菜");
        book1.setSurface("1.jpg");
        book1.setChapters(5);
        book1.setViews(3);
        book1.setTags("科学世界");
        books.put(book1.toJSONObject());

        Assert.assertEquals(books.toString(),userFavBookService.searchFav("天体观测","vagrant",0,2).toString());
    }
}