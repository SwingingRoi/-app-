package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/addbook")
    public void addBook(HttpServletRequest request, HttpServletResponse response){
        try{
            JSONObject newBook = new JSONObject(httpUtils.getStringParam(request));
            System.out.println(newBook.toString());
            String result = bookService.addBook(newBook);
            httpUtils.writeStringBack(response,result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/deletebooks")
    public void deleteBooks(HttpServletRequest request){
        try {
            JSONArray ids = new JSONArray(httpUtils.getStringParam(request));
            bookService.deleteBooks(ids);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/getbooks")
    public void getBooks(HttpServletResponse response, @RequestParam("from") int from, @RequestParam("size") int size){
        try{
            JSONArray result = bookService.getBooks(from,size);
            httpUtils.writeStringBack(response,result.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/getworks")
    public void getWorks(@RequestParam("account") String account,@RequestParam("from") int from,
                         @RequestParam("size") int size, HttpServletResponse response){
        try{
            JSONArray worksArray = bookService.getWorks(URLDecoder.decode(account, "UTF-8"),from,size);
            httpUtils.writeStringBack(response,worksArray.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/modifybook")
    public void modifyBook(@RequestParam("id") int bookid, HttpServletRequest request,
                           HttpServletResponse response){
        try{
            JSONObject info = new JSONObject(httpUtils.getStringParam(request));
            httpUtils.writeStringBack(response,bookService.modifyBook(bookid,info));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/audiobook/publishBook")
    public void publishBook(@RequestParam("id") int bookid){
        try{
            bookService.publishBook(bookid);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
