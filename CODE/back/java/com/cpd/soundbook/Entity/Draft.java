package com.cpd.soundbook.Entity;

import javax.persistence.*;

@Entity
@Table(name = "drafts")
public class Draft {
    private int bookid;
    private String draft;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    public Draft(){}

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

    public void setDraft(String draft) {
        this.draft = draft;
    }

    public String getDraft() {
        return draft;
    }
}
