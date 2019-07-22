package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublishBookController {

    @Autowired
    private BookService bookService;

    @RequestMapping("/audiobook/publishBook")
    public void publishBook(@RequestParam("id") int bookid){
        try{
            bookService.publishBook(bookid);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
