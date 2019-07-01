package com.cpd.soundbook.Entity;


import org.json.JSONObject;

import javax.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    private String name;
    private String intro;
    private String author;
    private String surface;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    public Book(){}

    public JSONObject toJSONObject(){
        JSONObject result = new JSONObject();
        try {
            result.put("id", id);
            result.put("name", name);
            result.put("author", author);
            result.put("intro", intro);
            result.put("surface", surface);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIntro() {
        return intro;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public String getSurface() {
        return surface;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
