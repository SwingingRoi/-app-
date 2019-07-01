package com.cpd.soundbook.DAO.DAOInterface;

import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Entity.UserFavBook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "userFavBookDAO")
public interface UserFavBookDAO {
    void addFav(UserFavBook userFavBook);

    void cancelFav(UserFavBook userFavBook);

    UserFavBook findFav(String account,int bookid);

    List<Book> findFavs(String account,int from,int size);

    List<Book> searchFavs(String search, String account, int from, int size);
}
