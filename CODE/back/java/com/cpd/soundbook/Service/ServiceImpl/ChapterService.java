package com.cpd.soundbook.Service.ServiceImpl;

import com.cpd.soundbook.DAO.DAOInterface.ChapterDAO;
import com.cpd.soundbook.DAO.DAOInterface.DraftDAO;
import com.cpd.soundbook.Entity.Chapter;
import com.cpd.soundbook.MongoDB.MongoDBInter;
import com.cpd.soundbook.TextToSpeech;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ChapterService implements com.cpd.soundbook.Service.ServiceInterface.ChapterService {
    @Autowired
    private ChapterDAO chapterDAO;

    @Autowired
    private DraftDAO draftDAO;

    @Autowired
    private TextToSpeech tts;

    @Autowired
    private MongoDBInter mongoDAO;

    @Override
    public void storeChapter(JSONObject chapter) {
        try{
            Chapter c = new Chapter();
            c.setBookid(chapter.getInt("bookid"));
            c.setTitle(chapter.getString("title"));
            c.setContent(chapter.getString("content"));
            c.setSpeechPath(chapter.getString("speechPath"));
            //c.setSpeechtime(chapter.getString("time"));
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

    @Override
    public void deleteChapters(JSONObject ids) {
        try {
            int bookid = ids.getInt("bookid");
            JSONArray idArray = ids.getJSONArray("ids");
            chapterDAO.deleteChapters(bookid,idArray);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject getChapterByID(int id) {
        Chapter chapter = chapterDAO.getChapterByID(id);
        JSONObject result = new JSONObject();
        try{
            result.put("id",id);
            result.put("title",chapter.getTitle());
            result.put("content",chapter.getContent());
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void modifyChapter(JSONObject chapter) {
        try{
            chapterDAO.modifyChapter(chapter.getInt("id"),chapter.getString("title"),chapter.getString("content"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public File textToSpeech(String text) {
        return tts.translate(text);
    }

    @Override
    public void storeSpeech(File speech) {
        try{
            mongoDAO.saveFile(speech);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
