package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.Chapter;
import com.cpd.soundbook.Repository.BookRepository;
import com.cpd.soundbook.Repository.ChapterRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ChapterDAO implements com.cpd.soundbook.DAO.DAOInterface.ChapterDAO {
    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManagerFactory factory;

    @Override
    public void storeChapter(Chapter chapter) {
        chapterRepository.save(chapter);
        bookRepository.increaChapter(chapter.getBookid());
    }


    @Override
    public List<Chapter> getChapters(int bookid, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Chapter> chapters = new ArrayList<>();

        try{
            String hql = "from com.cpd.soundbook.Entity.Chapter where bookid=:bookid";
            Query query = session.createQuery(hql);
            query.setParameter("bookid",bookid);

            query.setFirstResult(from);
            query.setMaxResults(size);

            chapters = (List<Chapter>) query.list();
            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return chapters;
    }

    @Override
    public void deleteChapters(int bookid,JSONArray idArray) {
        try{
            for(int i=0;i<idArray.length();i++){
                chapterRepository.deleteById(idArray.getJSONObject(i).getInt("id"));
            }
            bookRepository.decreaChapter(idArray.length(),bookid);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Chapter getChapterByID(int id) {
        return chapterRepository.findChapterById(id);
    }


    @Override
    public void modifyChapter(JSONObject chapter) {
        chapterRepository.modifyChapter(chapter.getInt("id"),
                chapter.getString("title"),
                chapter.getString("content"),
                chapter.getString("speechPath"),
                chapter.getString("length"));
    }
}
