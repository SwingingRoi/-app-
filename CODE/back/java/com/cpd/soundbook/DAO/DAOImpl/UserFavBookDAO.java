package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Entity.UserFavBook;
import com.cpd.soundbook.Repository.UserFavBookRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserFavBookDAO implements com.cpd.soundbook.DAO.DAOInterface.UserFavBookDAO {

    @Autowired
    private UserFavBookRepository userFavBookRepository;

    @Autowired
    private EntityManagerFactory factory;

    @Override
    public void addFav(UserFavBook userFavBook) {
        userFavBookRepository.save(userFavBook);
    }

    @Override
    public void cancelFav(UserFavBook userFavBook) {
        userFavBookRepository.cancelFav(userFavBook.getAccount(),userFavBook.getBookid());
    }

    @Override
    public UserFavBook findFav(String account, int bookid) {
        return userFavBookRepository.findByAccountAndBookid(account,bookid);
    }

    @Override
    public List<Book> findFavs(String account, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> results = new ArrayList<>();

        try{
            String hql = "select b from com.cpd.soundbook.Entity.UserFavBook UFB,com.cpd.soundbook.Entity.Book b where UFB.account=:account " +
                    "and UFB.bookid = b.id";
            Query query = session.createQuery(hql);
            query.setParameter("account",account);

            query.setFirstResult(from);
            query.setMaxResults(size);

            results = (List<Book>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return results;
    }

    @Override
    public List<Book> searchFavs(String search, String account, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> results = new ArrayList<>();

        try{
            String hql = "select b from com.cpd.soundbook.Entity.UserFavBook UFB ,com.cpd.soundbook.Entity.Book b " +
                    "where UFB.account=:account and UFB.bookid=b.id and b.name=:search";
            Query query = session.createQuery(hql);
            query.setParameter("account",account);
            query.setParameter("search",search);

            query.setFirstResult(from);
            query.setMaxResults(size);

            results = (List<Book>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return results;
    }
}
