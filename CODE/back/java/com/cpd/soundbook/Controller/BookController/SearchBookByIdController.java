package com.cpd.soundbook.Controller.BookController;


import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


@RestController
public class SearchBookByIdController {

    @Autowired
    private BookService bookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getbookbyid")
    public void getBookById(@RequestParam("id") int bookid, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,bookService.searchBookById(bookid).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
