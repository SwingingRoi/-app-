package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.DAO.DAOImpl.BookDAO;
import com.cpd.soundbook.DAO.DAOImpl.UserBrowseBookDAO;
import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Entity.UserBrowseBook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBrowseBookService implements com.cpd.soundbook.Service.ServiceInterface.UserBrowseBookService {

    @Autowired
    private UserBrowseBookDAO userBrowseBookDAO;

    @Autowired
    private BookDAO bookDAO;

    @Override
    public void addRecord(JSONObject record) {
        try {
            UserBrowseBook userBrowseBook = new UserBrowseBook();
            userBrowseBook.setAccount(record.getString("account"));
            userBrowseBook.setBookid(record.getInt("id"));
            userBrowseBook.setTime(record.getString("time"));
            userBrowseBookDAO.addRecord(userBrowseBook);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public JSONArray getRecords(String account, int from, int size) {
        JSONArray result = new JSONArray();
        try{
            List<UserBrowseBook> recordsList = userBrowseBookDAO.getRecords(account,from,size);
            for(UserBrowseBook record : recordsList){
                Book book = bookDAO.findBookById(record.getBookid());
                JSONObject object = record.toJSONObject();
                object.put("name",book.getName());
                object.put("bookid",book.getId());
                result.put(object);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void deleteRecords(JSONArray ids) {
        userBrowseBookDAO.deleteRecords(ids);
    }

    @Override
    public void clearRecords(String account) {
        userBrowseBookDAO.clearRecords(account);
    }

    @Override
    public JSONArray searchRecords(String account, int day, int from, int size) {
        JSONArray result = new JSONArray();
        try{
            List<UserBrowseBook> recordList = userBrowseBookDAO.searchRecords(account, day, from, size);
            for(UserBrowseBook u : recordList){
                Book book = bookDAO.findBookById(u.getBookid());
                JSONObject object = u.toJSONObject();
                object.put("name",book.getName());
                object.put("bookid",book.getId());
                result.put(object);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
