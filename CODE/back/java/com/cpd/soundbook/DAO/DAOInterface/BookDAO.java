package com.cpd.soundbook.DAO.DAOInterface;

import com.cpd.soundbook.Entity.Book;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "bookDAO")
public interface BookDAO {
    void addBook(Book book);

    List<Book> getBooks(int from,int size);

    List<Book> getWorks(String account,int from,int size);

    List<Book> searchWorkByTitle(String author,String title,int from,int size);

    List<Book> searchBooks(String search,int from,int size);

    void updateBook(Book book);

    Book findBookById(int id);

    void deleteBooks(JSONArray ids);

    List<Book> findBookByTags(List<String> tags,int from,int size);
}
