package com.cpd.soundbook.Controller.FavoriteController;

import com.cpd.soundbook.HttpUtils;
import com.cpd.soundbook.Service.ServiceImpl.UserFavBookService;
import com.cpd.soundbook.Service.ServiceImpl.UserService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
public class CancelFavController {
    @Autowired
    private UserFavBookService userFavBookService;

    @Autowired
    private HttpUtils httpUtils;

    @RequestMapping("/audiobook/cancelFavorite")
    public void cancelFav(@RequestParam("account") String account,
                          HttpServletRequest request,
                          HttpServletResponse response){
        try{
            JSONArray ids = new JSONArray(httpUtils.getStringParam(request));
            httpUtils.writeStringBack(response,userFavBookService.cancelFav(URLDecoder.decode(account, "UTF-8"),ids));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
