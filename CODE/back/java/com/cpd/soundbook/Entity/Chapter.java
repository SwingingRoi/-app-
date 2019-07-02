package com.cpd.soundbook.Entity;

import javax.persistence.*;

@Entity
@Table(name = "chapters")
public class Chapter {
    private int bookid;
    private String content;
    private String title;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    public Chapter(){}

    public void setBookid(int bookid) {
        this.bookid = bookid;
    }

    public int getBookid() {
        return bookid;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
