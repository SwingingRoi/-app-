package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.DAO.DAOImpl.BookDAO;
import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.MongoDB.MongoDBInter;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class BookService implements com.cpd.soundbook.Service.ServiceInterface.BookService {

    @Autowired
    private BookDAO bookDAO;

    @Autowired
    private MongoDBInter mongoDAO;

    @Override
    public String addBook(JSONObject newBook) {
        String result="success";
        try {
            Book book = new Book();
            book.setAuthor(newBook.getString("author"));
            book.setIntro(newBook.getString("intro"));
            book.setName(newBook.getString("name"));
            book.setSurface(newBook.getString("surface"));
            book.setTags(newBook.getString("tags"));
            bookDAO.addBook(book);
        }catch (Exception e){
            e.printStackTrace();
            result = "fail";
            return result;
        }
        return result;
    }

    @Override
    public JSONArray getBooks(int from, int size) {
        List<Book> works = bookDAO.getBooks(from,size);
        JSONArray results = new JSONArray();

        try{
            for(Book book :works){
                results.put(book.toJSONObject());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public JSONArray getWorks(String account, int from, int size) {
        List<Book> works = bookDAO.getWorks(account,from,size);
        JSONArray results = new JSONArray();

        try{
            for(Book book :works){
                results.put(book.toJSONObject());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public JSONArray searchBook(String search,int from,int size) {
        JSONArray results = new JSONArray();
        List<Book> books = bookDAO.searchBooks(search,from,size);
        try{
            for(Book book : books){
                results.put(book.toJSONObject());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public JSONArray searchWorkByTitle(String author,String title, int from, int size) {
        JSONArray results = new JSONArray();
        List<Book> books = bookDAO.searchWorkByTitle(author,title,from,size);
        try{
            for(Book book : books){
                results.put(book.toJSONObject());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public JSONObject searchBookById(int id) {
        Book book = bookDAO.findBookById(id);
        if(book == null){
            book = new Book();
            book.setId(-1);
        }
        return book.toJSONObject();
    }

    @Override
    public String modifyBook(int id, JSONObject info) {
        String result = "success";

        try{
            Book book = new Book();
            book.setId(id);
            book.setName(info.getString("name"));
            book.setIntro(info.getString("intro"));
            book.setSurface(info.getString("surface"));
            bookDAO.updateBook(book);
        }catch (Exception e){
            e.printStackTrace();
            result = "fail";
        }
        return result;
    }

    @Override
    public String saveSurface(File file) {
        String result;
        try {
            mongoDAO.saveFile(file);
            result = file.getName();
        }catch (Exception e){
            e.printStackTrace();
            result = "fail";
        }
        return result;
    }

    @Override
    public GridFSDBFile getSurface(int id) {
        Book book = bookDAO.findBookById(id);
        if(book==null) return null;
        if(book.getSurface()==null) return null;
        return mongoDAO.getFileByName(book.getSurface());
    }

    @Override
    public void deleteSurface(String fileanme) {
        mongoDAO.deleteFile(fileanme);
    }

    @Override
    public void deleteBooks(JSONArray ids) {
        bookDAO.deleteBooks(ids);
    }

    @Override
    public void publishBook(int bookid) {
        bookDAO.publishBook(bookid);
    }
}
