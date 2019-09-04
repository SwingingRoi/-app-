package com.cpd.soundbook.Controller.FavoriteController;


import com.cpd.soundbook.HttpUtils.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.UserFavBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class CheckFavoriteController {

    @Autowired
    private UserFavBookService userFavBookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/checkFav")
    public void checkFav(@RequestParam("account") String account, @RequestParam("id") int bookid,
                         HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,userFavBookService.checkFav(URLDecoder.decode(account, "UTF-8"),bookid));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
