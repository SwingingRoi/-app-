package com.cpd.soundbook.DAO.DAOInterface;

import com.cpd.soundbook.Entity.UserBrowseBook;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "userBrowseBookDAO")
public interface UserBrowseBookDAO {
    void addRecord(UserBrowseBook userBrowseBook);

    List<UserBrowseBook> getRecords(String account,int from,int size);

    void deleteRecords(JSONArray ids);

    void clearRecords(String account);

    List<UserBrowseBook> searchRecords(String account,int day,int from,int size);
}
