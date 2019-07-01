package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class GetWorksController {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private BookService bookService;

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
}
