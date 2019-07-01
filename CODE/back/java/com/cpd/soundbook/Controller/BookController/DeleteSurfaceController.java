package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.Service.ServiceImpl.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteSurfaceController {

    @Autowired
    private BookService bookService;

    @RequestMapping("/audiobook/deletesurface")
    public void deleteSurface(@RequestParam("filename") String filename){
            bookService.deleteSurface(filename);
    }
}
