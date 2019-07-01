package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import com.cpd.soundbook.Service.ServiceInterface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class SaveSurfaceController {

    @Autowired
    private BookService bookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/saveSurface")
    public void saveAvatar(HttpServletRequest request, HttpServletResponse response){
        File surface = httpUtils.getFileParam(request);
        System.out.println(surface.getName());
        httpUtils.writeStringBack(response,bookService.saveSurface(surface));
    }
}
