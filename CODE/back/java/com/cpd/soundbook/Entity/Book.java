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
    private int chapters;
    private int views;
    private String tags;
    private boolean publish;

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
            result.put("chapters",chapters);
            result.put("views",views);
            result.put("tags",tags);
            result.put("publish",publish);
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

    public void setChapters(int chapters) {
        this.chapters = chapters;
    }

    public int getChapters() {
        return chapters;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getViews() {
        return views;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isPublish() {
        return publish;
    }
}
