package com.cpd.soundbook.Service.ServiceInterface;

import org.json.JSONArray;
import org.springframework.stereotype.Component;

@Component(value = "userFavBookService")
public interface UserFavBookService {
    String addFav(String account,int bookid);

    String cancelFav(String account,JSONArray ids);

    String checkFav(String account,int bookid);

    JSONArray getFav(String account,int from,int size);

    JSONArray searchFav(String search,String account,int from,int size);
}
