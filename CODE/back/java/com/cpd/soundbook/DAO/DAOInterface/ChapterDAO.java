package com.cpd.soundbook.DAO.DAOInterface;

import com.cpd.soundbook.Entity.Chapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "chapterDAO")
public interface ChapterDAO {
    void storeChapter(Chapter chapter);

    List<Chapter> getChapters(int bookid,int from,int size);
}
