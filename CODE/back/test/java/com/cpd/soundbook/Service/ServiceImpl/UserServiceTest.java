package com.cpd.soundbook.Service.ServiceImpl;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class UserServiceTest {
    @Autowired
    private UserService userService;

    private JSONObject generateUser(String account, String password){
        JSONObject user = new JSONObject();
        try{
            user.put("account",account);
            user.put("password",password);
        }catch (Exception e){
            e.printStackTrace();
        }
        return user;
    }

    @Test
    public void sign() {
        try{
            //正确注册
            JSONObject user1 = new JSONObject();
            user1.put("account","test1");
            user1.put("password","1111111");
            user1.put("email","test1@qq.com");
            user1.put("phone","88888888");
            user1.put("name","test");
            user1.put("gender","male");
            Assert.assertEquals("success",userService.sign(user1));

            //用户名重复
            JSONObject user2 = new JSONObject();
            user2.put("account","vagrant");
            user2.put("password","1111111");
            user2.put("email","testtest@qq.com");
            user2.put("phone","1234567891011");
            user2.put("name","test");
            user2.put("gender","male");
            Assert.assertEquals("accountDul",userService.sign(user2));

            //邮箱重复
            JSONObject user3 = new JSONObject();
            user3.put("account","test2");
            user3.put("password","1111111");
            user3.put("email","492556292@qq.com");
            user3.put("phone","1234567894856");
            user3.put("name","test");
            user3.put("gender","male");
            Assert.assertEquals("emailDul",userService.sign(user3));

            //手机号重复
            JSONObject user4 = new JSONObject();
            user4.put("account","test3");
            user4.put("password","1111111");
            user4.put("email","test3@qq.com");
            user4.put("phone","18873413219");
            user4.put("name","test");
            user4.put("gender","male");
            Assert.assertEquals("phoneDul",userService.sign(user4));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void log() {
        try {
            //合法登陆
            JSONObject user1 = generateUser("vagrant","dtmdshdbsbm");
            Assert.assertSame("success",userService.log(user1));

            //用户名为空
            JSONObject user2 = generateUser("","11111111");
            Assert.assertSame("none",userService.log(user2));

            //密码为空
            JSONObject user3 = generateUser("vagrant","");
            Assert.assertSame("fail",userService.log(user3));

            //用户名不存在
            JSONObject user4 = generateUser("不存在","dtmdshdbsbm");
            Assert.assertSame("none",userService.log(user4));

            //用户名与密码不匹配
            JSONObject user5 = generateUser("vagrant","dtmdshdbsb");
            Assert.assertSame("fail",userService.log(user5));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void activate() {
        try{
            //正确激活
            Assert.assertSame("success",userService.activate("vagrant"));

            //用户名不存在
            Assert.assertSame("fail",userService.activate("不存在"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getInfo() {
        try{
            //正确的用户名
            String account = "vagrant";
            Assert.assertEquals(account,userService.getInfo(account).getString("account"));

            //不存在的用户名
            Assert.assertSame(null,userService.getInfo("不存在"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void updateUser() {
        try{
            //正确修改信息
            JSONObject user1 = new JSONObject();
            user1.put("oldAccount","new");
            user1.put("account","new");
            user1.put("email","49255629@qq.com");
            user1.put("password","dtmdshdbsb");
            user1.put("name","cp");
            user1.put("gender","male");
            Assert.assertEquals("success",userService.updateUser(user1));

            //不存在的账号
            JSONObject user2 = new JSONObject();
            user2.put("oldAccount","不存在");
            user2.put("account","new");
            user2.put("email","49255629@qq.com");
            user2.put("password","dtmdshdbsb");
            user2.put("name","cp");
            user2.put("gender","male");
            Assert.assertEquals("none",userService.updateUser(user2));

            //修改后的用户名重复
            JSONObject user3 = new JSONObject();
            user3.put("oldAccount","new");
            user3.put("account","vagrant");
            user3.put("email","49255629@qq.com");
            user3.put("password","dtmdshdbsb");
            user3.put("name","cp");
            user3.put("gender","male");
            Assert.assertEquals("accountDul",userService.updateUser(user3));

            //修改后的邮箱重复
            JSONObject user4 = new JSONObject();
            user4.put("oldAccount","new");
            user4.put("account","newuser");
            user4.put("email","492556292@qq.com");
            user4.put("password","dtmdshdbsb");
            user4.put("name","cp");
            user4.put("gender","male");
            Assert.assertEquals("emailDul",userService.updateUser(user4));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void saveAvatar() {
        try{
            //正确存储文件
            File file1 = new File(System.getProperty("user.dir")+"\\"+System.currentTimeMillis()+".png");
            Assert.assertEquals(file1.getName(),userService.saveAvatar(file1));

            //存储空文件
            File file2 = null;
            Assert.assertEquals("fail",userService.saveAvatar(file2));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getAvatar() {
        try{
            //存在头像
            String account1 = "vagrant";
            String filename = "1558945306226.png";
            Assert.assertEquals(filename,userService.getAvatar(account1).getFilename());

            //不存在头像
            String account2 = "jhd8400";
            Assert.assertNull(userService.getAvatar(account2));

            //不存在该用户
            String account3 = "不存在";
            Assert.assertNull(userService.getAvatar(account3));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}