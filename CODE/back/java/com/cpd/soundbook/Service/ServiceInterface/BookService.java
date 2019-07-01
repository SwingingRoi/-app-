package com.cpd.soundbook.Service.ServiceInterface;

import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;

@Component(value = "bookService")
public interface BookService {
    String addBook(JSONObject newBook);

    String saveSurface(File file);

    JSONArray getBooks(int from,int size);

    JSONArray getWorks(String account,int from,int size);

    JSONArray searchBook(String search,int from,int size);

    JSONArray searchWorkByTitle(String author,String title,int from,int size);

    JSONObject searchBookById(int id);

    String modifyBook(int id,JSONObject info);

    GridFSDBFile getSurface(int id);

    void deleteSurface(String fileanme);

    void deleteBooks(JSONArray ids);
}
