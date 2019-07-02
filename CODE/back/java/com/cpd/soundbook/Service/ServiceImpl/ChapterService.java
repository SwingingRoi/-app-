package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.DAO.DAOInterface.ChapterDAO;
import com.cpd.soundbook.DAO.DAOInterface.DraftDAO;
import com.cpd.soundbook.Entity.Chapter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterService implements com.cpd.soundbook.Service.ServiceInterface.ChapterService {
    @Autowired
    private ChapterDAO chapterDAO;

    @Autowired
    private DraftDAO draftDAO;

    @Override
    public void storeChapter(JSONObject chapter) {
        try{
            Chapter c = new Chapter();
            c.setBookid(chapter.getInt("bookid"));
            c.setTitle(chapter.getString("title"));
            c.setContent(chapter.getString("content"));
            chapterDAO.storeChapter(c);
            draftDAO.deleteDraftByBookid(chapter.getInt("bookid"));//删除上次的草稿
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public JSONArray getChapters(int bookid, int from, int size) {
        JSONArray result = new JSONArray();
        List<Chapter> chapters = chapterDAO.getChapters(bookid, from, size);

        try {
                for (Chapter chapter : chapters) {
                    JSONObject object = new JSONObject();
                    object.put("id", chapter.getId());
                    object.put("bookid", chapter.getBookid());
                    object.put("title", chapter.getTitle());
                    result.put(object);
                }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
