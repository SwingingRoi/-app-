package com.cpd.soundbook.Controller.BookController;


import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DeleteBooksController {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private BookService bookService;

    @RequestMapping("/audiobook/deletebooks")
    public void deleteBooks(HttpServletRequest request){
        try {
            JSONArray ids = new JSONArray(httpUtils.getStringParam(request));
            bookService.deleteBooks(ids);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
