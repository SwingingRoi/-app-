package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.DAO.DAOInterface.UserDAO;
import com.cpd.soundbook.Entity.User;
import com.cpd.soundbook.Entity.UserFavBook;
import com.cpd.soundbook.MongoDB.MongoDBInter;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class UserService implements com.cpd.soundbook.Service.ServiceInterface.UserService {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MongoDBInter mongoDAO;

    @Override
    public String sign(JSONObject newUser) {
        String result="success";//注册成功
        try {
            User u1 = userDAO.findUserByAccount(newUser.getString("account"));
            if(u1!=null && u1.getAccount()!=null){
                result = "accountDul";//用户名重复
                return result;
            }
            User u2 = userDAO.findUserByEmail(newUser.getString("email"));
            if(u2!=null && u2.getAccount()!=null){
                result = "emailDul";//邮箱重复
                return result;
            }
            User u3 = userDAO.findUserByPhone(newUser.getString("phone"));
            if(u3!=null && u3.getAccount()!=null){
                result = "phoneDul";//手机号重复
                return result;
            }
            User user = new User();
            user.setAccount(newUser.getString("account"));
            user.setPassword(newUser.getString("password"));
            user.setName(newUser.getString("name"));
            user.setEmail(newUser.getString("email"));
            user.setPhone(newUser.getString("phone"));
            user.setGender(newUser.getString("gender"));
            userDAO.addUser(user);
        }catch (Exception e){
            e.printStackTrace();
            result = "fail";//注册失败
        }
        return result;
    }

    @Override
    public String log(JSONObject user) {
        String result="";
        try{
            User u = userDAO.findUserByAccount(user.getString("account"));
            if(u==null){
                result = "none";//账号不存在
            }
            else if(u.getPassword().equals(user.getString("password"))){
                if(!u.isActivated()){
                    result = u.getEmail();//账号未激活，返回邮箱
                }
                else {
                    result = "success";//登录成功
                }
            }
            else {
                result = "fail";//用户名或密码错误
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String activate(String account) {
        if(userDAO.activateUser(account)){
            return "success";
        }
        else return "fail";
    }

    @Override
    public JSONObject getInfo(String account) {
        JSONObject result = null;
        try{
            User user = userDAO.findUserByAccount(account);
            if(user!=null) {
                result = user.toJSONObject();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String updateUser(JSONObject param) {
        String result="success";
        try{
            User oldUser = userDAO.findUserByAccount(param.getString("oldAccount"));
            User ifAccountDul = new User();
            if(!param.getString("oldAccount").equals(param.getString("account"))) {
                ifAccountDul = userDAO.findUserByAccount(param.getString("account"));
            }
            User ifEmailDul = userDAO.findUserByEmail(param.getString("email"));
            if(ifAccountDul!=null && ifAccountDul.getAccount()!=null && ifAccountDul.getId()!=oldUser.getId()){
                result="accountDul";
            }
            else if(ifEmailDul!=null && ifEmailDul.getAccount()!=null && ifEmailDul.getId()!=oldUser.getId()){
                result = "emailDul";
            }
            else {
                oldUser.setAccount(param.getString("account"));
                oldUser.setPassword(param.getString("password"));
                oldUser.setName(param.getString("name"));
                oldUser.setGender(param.getString("gender"));
                oldUser.setEmail(param.getString("email"));
                userDAO.updateUser(oldUser);
            }
        }catch (Exception e){
            e.printStackTrace();
            result="fail";
            return result;
        }
        return result;
    }

    @Override
    public String saveAvatar(File file) {
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
    public void saveAvatarName(JSONObject param){
        try{
            User user = userDAO.findUserByID(param.getInt("id"));
            mongoDAO.deleteFile(user.getAvatar());//删除旧头像
            user.setAvatar(param.getString("avatar"));
            userDAO.updateUser(user);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public GridFSDBFile getAvatar(String account) {
        User user = userDAO.findUserByAccount(account);
        if(user==null) return null;
        if(user.getAvatar()==null) return null;
        return mongoDAO.getFile(user.getAvatar());
    }
}
