package com.cpd.soundbook.DAO.DAOImpl;


import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Repository.BookRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;


@Repository
public class BookDAO implements com.cpd.soundbook.DAO.DAOInterface.BookDAO {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManagerFactory factory;

    @Transactional
    @Override
    public void addBook(Book book) {
        bookRepository.save(book);
    }

    @Transactional
    @Override
    public List<Book> getBooks(int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.Book";
            Query query = session.createQuery(hql);

            query.setFirstResult(from);
            query.setMaxResults(size);

            books = (List<Book>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return books;
    }

    @Transactional
    @Override
    public List<Book> getWorks(String account, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.Book where author=:author";
            Query query = session.createQuery(hql);
            query.setParameter("author",account);

            query.setFirstResult(from);
            query.setMaxResults(size);

            books = (List<Book>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return books;
    }

    @Transactional
    @Override
    public List<Book> searchBooks(String search, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.Book where author=:search or name=:search";
            Query query = session.createQuery(hql);
            query.setParameter("search",search);

            query.setFirstResult(from);
            query.setMaxResults(size);

            books = (List<Book>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return books;
    }

    @Transactional
    @Override
    public List<Book> searchWorkByTitle(String author,String title, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.Book where name=:name and author=:author";
            Query query = session.createQuery(hql);
            query.setParameter("name",title);
            query.setParameter("author",author);

            query.setFirstResult(from);
            query.setMaxResults(size);

            books = (List<Book>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
            session.close();
        }
        return books;
    }

    @Transactional
    @Override
    public Book findBookById(int id) {
        return bookRepository.findBookById(id);
    }

    @Transactional
    @Override
    public void updateBook(Book book) {
        bookRepository.modifyInfo(book.getName(),book.getIntro(),book.getSurface(),book.getId());
    }

    @Transactional
    @Override
    public void deleteBooks(JSONArray ids) {
        try {
            for (int i = 0; i < ids.length(); i++) {
                bookRepository.deleteById(ids.getJSONObject(i).getInt("id"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
