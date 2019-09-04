package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Entity.UserBrowseBook;
import com.cpd.soundbook.Repository.BookRepository;
import com.cpd.soundbook.Repository.UserBrowseBookRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Repository
public class UserBrowseBookDAO implements com.cpd.soundbook.DAO.DAOInterface.UserBrowseBookDAO {

    final private long ONE_DAY = 86400000;

    @Autowired
    private EntityManagerFactory factory;

    @Autowired
    private UserBrowseBookRepository userBrowseBookRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public void addRecord(UserBrowseBook userBrowseBook) {
        Book book = bookRepository.findBookById(userBrowseBook.getBookid());
        if(!book.getAuthor().equals(userBrowseBook.getAccount())) bookRepository.increaView(userBrowseBook.getBookid());
        if(userBrowseBookRepository.findRecord(userBrowseBook.getAccount(),userBrowseBook.getBookid(),userBrowseBook.getTime()) == null) {
            userBrowseBookRepository.save(userBrowseBook);
        }
    }

    @Override
    public List<UserBrowseBook> getRecords(String account, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<UserBrowseBook> records = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account order by id desc ";
            Query query = session.createQuery(hql);
            query.setParameter("account",account);

            query.setFirstResult(from);
            query.setMaxResults(size);


            records = (List<UserBrowseBook>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return records;
    }

    @Override
    public void deleteRecords(JSONArray ids) {
        try {
            for (int i = 0; i < ids.length(); i++) {
                userBrowseBookRepository.deleteById(ids.getJSONObject(i).getInt("id"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void clearRecords(String account) {
        userBrowseBookRepository.clearRecords(account);
    }

    @Override
    public List<UserBrowseBook> searchRecords(String account, int day, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<UserBrowseBook> records = new ArrayList<>();

        try{
            String hql;
            Query query;

            long timeNow = System.currentTimeMillis();
            long todayStartTime = timeNow - (timeNow + TimeZone.getDefault().getRawOffset())% (1000*3600*24);
            long yesterdayStartTime = todayStartTime - ONE_DAY;
            long beforeyesStartTime = yesterdayStartTime - ONE_DAY;

            if(day == 0){
                hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account and time>=:start and time<=:till order by id desc ";
                query = session.createQuery(hql);
                query.setParameter("account",account);
                query.setParameter("start",String.valueOf(todayStartTime));
                query.setParameter("till",String.valueOf(timeNow));
            }
            else if(day == 1){
                hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account and time>=:start and time<=:till order by id desc ";
                query = session.createQuery(hql);
                query.setParameter("account",account);
                query.setParameter("start",String.valueOf(yesterdayStartTime));
                query.setParameter("till",String.valueOf(todayStartTime));
            }else if(day == 2){
                hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account and time>=:start and time<=:till order by id desc ";
                query = session.createQuery(hql);
                query.setParameter("account",account);
                query.setParameter("start",String.valueOf(beforeyesStartTime));
                query.setParameter("till",String.valueOf(yesterdayStartTime));
            }else {
                hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account and time<=:till order by id desc ";
                query = session.createQuery(hql);
                query.setParameter("account",account);
                query.setParameter("till",String.valueOf(beforeyesStartTime));
            }

            query.setFirstResult(from);
            query.setMaxResults(size);


            records = (List<UserBrowseBook>) query.list();

            session.close();
        }catch (Exception e){
            session.close();
            e.printStackTrace();
        }
        return records;
    }
}
