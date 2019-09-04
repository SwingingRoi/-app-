package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.DAO.DAOImpl.BookDAO;
import com.cpd.soundbook.DAO.DAOImpl.UserFavBookDAO;
import com.cpd.soundbook.DAO.DAOInterface.UserDAO;
import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Entity.User;
import com.cpd.soundbook.MongoDB.MongoDBInter;
import com.cpd.soundbook.TopKtags.TopKTags;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements com.cpd.soundbook.Service.ServiceInterface.UserService {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MongoDBInter mongoDAO;

    @Autowired
    private UserFavBookDAO userFavBookDAO;

    @Autowired
    private UserBrowseBookService userBrowseBookService;

    @Autowired
    private BookDAO bookDAO;

    @Autowired
    private TopKTags topKTags;

    final private int INFINITE = 999999999;

    final private int FAV_WEIGHT = 8;//收藏在推荐中的比重
    final private int HISTORY_WEIGHT = 2;//浏览历史在推荐中的比重
    final private int TOP_NUMBER = 3;//获取权重排名前k的标签

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
        String result = "";
        if(userDAO.findUserByAccount(account) == null) result = "fail";
        else {
            if(userDAO.activateUser(account)) result = "success";
        }

        return result;
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
            if(oldUser == null){
                result = "none";
                return result;
            }
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
            if(file == null) {
                result = "fail";
            }
            else {
                mongoDAO.saveFile(file);
                result = file.getName();
            }
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
        return mongoDAO.getFileByName(user.getAvatar());
    }

    @Override
    public JSONArray getRecommend(String account, int from, int size) {
        JSONArray result = new JSONArray();

        try {
            List<Book> favBooks = userFavBookDAO.findFavs(account, from, INFINITE);//找到用户收藏的所有书本

        /*
        browse:{'name':bookname,'bookid':bookid,'tags':tags,'account':account,'id':id,'time':time}
         */
            JSONArray browses = userBrowseBookService.getRecords(account, from, 20);//找最近20条浏览记录

            HashMap<String, Integer> inputs = new HashMap<>();

            //计算收藏夹中各标签的权重
            for (Book book : favBooks) {
                String[] tags = book.getTags().split(" ");//获取书本tags
                for (String tag : tags) {//若tag已在inputs中，更新权重
                    if (inputs.containsKey(tag)) {
                        int oldWeight = inputs.get(tag);
                        inputs.put(tag, oldWeight + FAV_WEIGHT);
                    } else {//否则向inputs中插入标签
                        inputs.put(tag, FAV_WEIGHT);
                    }
                }
            }

            //计算最近20条浏览记录中各标签的权重
            for (int i = 0; i < browses.length(); i++) {
                String[] tags = browses.getJSONObject(i).getString("tags").split(" ");
                for (String tag : tags) {//若tag已在inputs中，更新权重
                    if (inputs.containsKey(tag)) {
                        int oldWeight = inputs.get(tag);
                        inputs.put(tag, oldWeight + HISTORY_WEIGHT);
                    } else {//否则向inputs中插入标签
                        inputs.put(tag, HISTORY_WEIGHT);
                    }
                }
            }

            List<Map.Entry<String, Integer>> topK = topKTags.getTopKTags(inputs, TOP_NUMBER);
            List<String> kTags = new ArrayList<>();


            /*
            topK.size() == 0对应两种情况：
            1、新建的用户，没有收藏和浏览记录.
            2、老用户，但清空了收藏夹和历史记录.
            此时的推荐按所有书本收听量从多到少进行推荐
             */
            if(topK.size() ==0){
                List<Book> books = bookDAO.findAllBookByViews(from, size);
                for(Book book : books){
                    result.put(book.toJSONObject());
                }
            }else {
                for (Map.Entry<String, Integer> entry : topK) kTags.add(entry.getKey());

                List<Book> books = bookDAO.findBookByTags(kTags,from,size);
                for(Book book : books){
                    result.put(book.toJSONObject());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
