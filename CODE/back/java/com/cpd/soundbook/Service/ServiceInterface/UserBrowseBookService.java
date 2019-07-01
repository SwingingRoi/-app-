package com.cpd.soundbook.Service.ServiceInterface;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component(value = "userBrowseBookService")
public interface UserBrowseBookService {
    void addRecord(JSONObject record);

    JSONArray getRecords(String account,int from,int size);

    void deleteRecords(JSONArray ids);

    void clearRecords(String account);

    JSONArray searchRecords(String account,int day,int from,int size);
}
