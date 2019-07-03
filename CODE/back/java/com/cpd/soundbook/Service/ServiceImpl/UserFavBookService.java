package com.cpd.soundbook.Service.ServiceImpl;


import com.cpd.soundbook.DAO.DAOInterface.UserFavBookDAO;
import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Entity.UserFavBook;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserFavBookService implements com.cpd.soundbook.Service.ServiceInterface.UserFavBookService {

    @Autowired
    private UserFavBookDAO userFavBookDAO;

    @Override
    public String addFav(String account, int bookid) {
        String result = "success";

        try{
            UserFavBook userFavBook = new UserFavBook();
            userFavBook.setAccount(account);
            userFavBook.setBookid(bookid);
            userFavBookDAO.addFav(userFavBook);
        }catch (Exception e){
            e.printStackTrace();
            result = "fail";
        }
        return result;
    }

    @Override
    public String cancelFav(String account, JSONArray ids) {
        String result = "success";

        try {
            for(int i=0;i<ids.length();i++) {
                UserFavBook userFavBook = new UserFavBook();
                userFavBook.setAccount(account);
                userFavBook.setBookid(ids.getJSONObject(i).getInt("id"));
                userFavBookDAO.cancelFav(userFavBook);
            }
        }catch (Exception e){
            e.printStackTrace();
            result = "fail";
        }
        return result;
    }

    @Override
    public String checkFav(String account, int bookid) {
        String result = "yes";
        UserFavBook userFavBook = userFavBookDAO.findFav(account,bookid);

        if(userFavBook == null) result = "no";
        return result;
    }

    @Override
    public JSONArray getFav(String account, int from, int size) {
        List<Book> favs = userFavBookDAO.findFavs(account,from,size);
        JSONArray  books = new JSONArray();

        for(Book book : favs){

            books.put(book.toJSONObject());
        }
        return books;
    }

    @Override
    public JSONArray searchFav(String search, String account, int from, int size) {
        List<Book> books = userFavBookDAO.searchFavs(search, account, from, size);
        JSONArray result = new JSONArray();
        for(Book book : books){
            result.put(book.toJSONObject());
        }
        return result;
    }
}
