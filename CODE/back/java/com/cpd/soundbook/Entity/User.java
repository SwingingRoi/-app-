package com.cpd.soundbook.Entity;

import org.json.JSONObject;

import javax.persistence.*;


@Entity
@Table(name = "users")
public class User {
    private String account;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private boolean activated;
    private String avatar;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;


    public JSONObject toJSONObject(){
        JSONObject result = new JSONObject();
        try{
                result.put("account",account);
                result.put("name",name);
                result.put("gender",gender);
                result.put("email",email);
                //String password = this.password.replaceAll(".","*");
                result.put("password",password);
                result.put("id",id);
        }catch (Exception e){
                e.printStackTrace();
            }
        return result;
    }

    public User (){}

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }
}
