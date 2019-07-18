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

import java.io.File;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class BookServiceTest {
    @Autowired
    private BookService bookService;

    @Test
    public void addBook() {
        try{
            //测试有声书增加
            JSONObject book=new JSONObject();
            book.put("author","test");
            book.put("intro","test");
            book.put("name","test");
            book.put("surface","test");
            book.put("tags","test");
            Assert.assertEquals("success",bookService.addBook(book));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getBooks() {
        //测试有声书获取
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

        Assert.assertEquals(books.toString(),bookService.getBooks(0,2).toString());
    }

    @Test
    public void getWorks() {
        //测试获取单个用户的作品
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

        Assert.assertEquals(books.toString(),bookService.getWorks("冰川日菜",0,1).toString());
    }

    @Test
    public void searchBook() {
        //测试有声书的搜索
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

        Assert.assertEquals(books.toString(),bookService.searchBook("天体观测",0,1).toString());
    }

    @Test
    public void searchWorkByTitle() {
        //测试根据作品标题搜索
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

        Assert.assertEquals(books.toString(),bookService.searchWorkByTitle("冰川日菜","天体观测",0,1).toString());
    }

    @Test
    public void searchBookById() {
        try {
            //测试根据书的id来搜索
            JSONObject book = new JSONObject();
            book.put("surface", "1.jpg");
            book.put("chapters",5);
            book.put("author","冰川日菜");
            book.put("intro","寻觅繁星");
            book.put("name","天体观测");
            book.put("id",1);
            book.put("views",3);
            book.put("tags","科学世界");

            Assert.assertEquals(book.toString(), bookService.searchBookById(1).toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void modifyBook() {
        try {
            //测试书本信息修改
            JSONObject intro=new JSONObject();
            intro.put("id",5);
            intro.put("name","modify");
            intro.put("intro","test");
            intro.put("surface","test");
            Assert.assertEquals("success",bookService.modifyBook(4,intro));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void saveSurface() {
        //正确存储文件
        File file1 = new File(System.getProperty("user.dir")+"\\"+System.currentTimeMillis()+".png");
        Assert.assertEquals(file1.getName(),bookService.saveSurface(file1));
    }

    @Test
    public void getSurface() {
        try {
            //测试不存在的书
            Assert.assertNull(bookService.getSurface(0));

            //测试没封面的书
            Assert.assertNull(bookService.getSurface(3));

            //正常测试
            String filename = "1.jpg";
            Assert.assertEquals(filename, bookService.getSurface(1).getFilename());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void  deleteSurface(){
        try {
            bookService.deleteSurface("1.jpg");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void deleteBooks() {
        try {
            JSONArray ids = new JSONArray();
            JSONObject id = new JSONObject();
            id.put("id", 5);
            ids.put(id);
            bookService.deleteBooks(ids);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}