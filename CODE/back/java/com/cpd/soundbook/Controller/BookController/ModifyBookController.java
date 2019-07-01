package com.cpd.soundbook.Controller.BookController;


import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ModifyBookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private HttpUtils httpUtils;

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
}
