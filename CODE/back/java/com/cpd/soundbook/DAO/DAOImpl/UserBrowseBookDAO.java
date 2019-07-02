package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.UserBrowseBook;
import com.cpd.soundbook.Repository.UserBrowseBookRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Repository
public class UserBrowseBookDAO implements com.cpd.soundbook.DAO.DAOInterface.UserBrowseBookDAO {

    @Autowired
    private EntityManagerFactory factory;

    @Autowired
    private UserBrowseBookRepository userBrowseBookRepository;

    @Transactional
    @Override
    public void addRecord(UserBrowseBook userBrowseBook) {
        if(userBrowseBookRepository.findRecord(userBrowseBook.getAccount(),userBrowseBook.getBookid(),userBrowseBook.getTime()) == null) {
            userBrowseBookRepository.save(userBrowseBook);
        }
    }

    @Transactional
    @Override
    public List<UserBrowseBook> getRecords(String account, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<UserBrowseBook> records = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account";
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

    @Transactional
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

    @Transactional
    @Override
    public void clearRecords(String account) {
        userBrowseBookRepository.clearRecords(account);
    }

    @Transactional
    @Override
    public List<UserBrowseBook> searchRecords(String account, int day, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<UserBrowseBook> records = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,0-day);
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
        String date = format.format(calendar.getTime());
        try{
            String hql;

            if(day<=2){
                hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account and time =:time";
            }
            else {
                hql = "from com.cpd.soundbook.Entity.UserBrowseBook  where account=:account and time <:time";
            }
            Query query = session.createQuery(hql);
            query.setParameter("account",account);
            query.setParameter("time",date);

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
