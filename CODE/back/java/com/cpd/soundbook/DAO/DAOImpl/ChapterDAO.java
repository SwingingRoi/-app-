package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.Chapter;
import com.cpd.soundbook.Repository.ChapterRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ChapterDAO implements com.cpd.soundbook.DAO.DAOInterface.ChapterDAO {
    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private EntityManagerFactory factory;

    @Transactional
    @Override
    public void storeChapter(Chapter chapter) {
        chapterRepository.save(chapter);
    }


    @Transactional
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
}
