package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AddBookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/addbook")
    public void addBook(HttpServletRequest request, HttpServletResponse response){
        try{
            JSONObject newBook = new JSONObject(httpUtils.getStringParam(request));
            String result = bookService.addBook(newBook);
            httpUtils.writeStringBack(response,result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
