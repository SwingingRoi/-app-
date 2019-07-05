package com.cpd.soundbook.DAO.DAOInterface;

import com.cpd.soundbook.Entity.Chapter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "chapterDAO")
public interface ChapterDAO {
    void storeChapter(Chapter chapter);

    List<Chapter> getChapters(int bookid,int from,int size);

    void deleteChapters(int bookid,JSONArray idArray);

    Chapter getChapterByID(int id);

    void modifyChapter(JSONObject chapter);
}
