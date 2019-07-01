package com.cpd.soundbook.Controller.FavoriteController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.UserFavBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class GetFavoriteController {

    @Autowired
    private UserFavBookService userFavBookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/getFavorites")
    public void getFavorite(@RequestParam("account") String account, @RequestParam("from") int from,
                            @RequestParam("size") int size, HttpServletResponse response){
        try{
            httpUtils.writeStringBack(response,userFavBookService.getFav(URLDecoder.decode(account, "UTF-8"), from, size).toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
