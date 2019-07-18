package com.cpd.soundbook.Service.ServiceInterface;

import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;

@Component(value = "userService")
public interface UserService {
    String sign(JSONObject newUser);

    String log(JSONObject user);

    String activate(String account);

    JSONObject getInfo(String account);

    String updateUser(JSONObject param);

    String saveAvatar(File file);

    void saveAvatarName(JSONObject param);

    GridFSDBFile getAvatar(String account);

    JSONArray getRecommend(String account,int from,int size);
}
