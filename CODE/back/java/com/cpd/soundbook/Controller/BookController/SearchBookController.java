package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class SearchBookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/searchbook")
    public void searchBook(@RequestParam("search") String search, @RequestParam("from") int from,
                           @RequestParam("size") int size, HttpServletResponse response){
        try{
            JSONArray result = bookService.searchBook(search,from,size);
            httpUtils.writeStringBack(response,result.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
