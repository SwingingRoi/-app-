package com.cpd.soundbook.Controller.BookController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceInterface.BookService;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@RestController
public class GetSurfaceController {

    @Autowired
    private BookService bookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getSurface")
    public void getSurface(@Param("id") int id, HttpServletResponse response){
        try {
            GridFSDBFile surface =bookService.getSurface(id);
            if(surface==null){
                httpUtils.writeStringBack(response,null);
            }
            else {
                OutputStream outputStream = response.getOutputStream();
                response.setContentType("image/*");
                surface.writeTo(outputStream);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
