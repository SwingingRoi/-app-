package com.cpd.soundbook.DAO.DAOImpl;


import com.cpd.soundbook.Entity.Book;
import com.cpd.soundbook.Repository.BookRepository;
import com.cpd.soundbook.Repository.ChapterRepository;
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

    @Override
    public void addBook(Book book) {
        bookRepository.save(book);
    }

    @Override
    public List<Book> getBooks(int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.Book where publish = true";
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

    @Override
    public List<Book> searchBooks(String search, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.Book where author like :search or name like :search and publish = true";
            Query query = session.createQuery(hql);
            query.setParameter("search","%" + search + "%");

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

    @Override
    public List<Book> searchWorkByTitle(String author,String title, int from, int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {

            String hql = "from com.cpd.soundbook.Entity.Book where name like :name and author=:author";
            Query query = session.createQuery(hql);
            query.setParameter("name","%" + title + "%");
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

    @Override
    public Book findBookById(int id) {
        return bookRepository.findBookById(id);
    }

    @Override
    public void updateBook(Book book) {
        bookRepository.modifyInfo(book.getName(),book.getIntro(),book.getSurface(),book.getTags(),book.getId());
    }

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


    @Override
    public List<Book> findBookByTags(List<String> tags,int from,int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try {
            String hql;
            Query query = null;

            switch (tags.size()){
                case 1:
                    hql = "from com.cpd.soundbook.Entity.Book where tags like :tag and publish = true order by views desc ";
                    query = session.createQuery(hql);
                    query.setParameter("tag","%" + tags.get(0) + "%");
                    break;
                case 2:
                    hql = "from com.cpd.soundbook.Entity.Book where (tags like :tag1 or " +
                            "tags like :tag2)  and publish = true order by views desc ";
                    query = session.createQuery(hql);
                    query.setParameter("tag1","%" + tags.get(0) + "%");
                    query.setParameter("tag2","%" + tags.get(1) + "%");
                    break;
                case 3:
                    hql = "from com.cpd.soundbook.Entity.Book where (tags like :tag1 or " +
                            "tags like :tag2 or tags like :tag3) and publish = true order by views desc ";
                    query = session.createQuery(hql);
                    query.setParameter("tag1","%" + tags.get(0) + "%");
                    query.setParameter("tag2","%" + tags.get(1) + "%");
                    query.setParameter("tag3","%" + tags.get(2) + "%");
                    break;
                case 4:
                    hql = "from com.cpd.soundbook.Entity.Book where (tags like :tag1 or " +
                            "tags like :tag2 or tags like :tag3 or tags like :tag4) and publish = true order by views desc ";
                    query = session.createQuery(hql);
                    query.setParameter("tag1","%" + tags.get(0) + "%");
                    query.setParameter("tag2","%" + tags.get(1) + "%");
                    query.setParameter("tag3","%" + tags.get(2) + "%");
                    query.setParameter("tag4","%" + tags.get(3) + "%");
                    break;
                case 5:
                    hql = "from com.cpd.soundbook.Entity.Book where (tags like :tag1 or " +
                            "tags like :tag2 or tags like :tag3 or tags like :tag4 or tags like :tag5) and publish = true order by views desc ";
                    query = session.createQuery(hql);
                    query.setParameter("tag1","%" + tags.get(0) + "%");
                    query.setParameter("tag2","%" + tags.get(1) + "%");
                    query.setParameter("tag3","%" + tags.get(2) + "%");
                    query.setParameter("tag4","%" + tags.get(3) + "%");
                    query.setParameter("tag5","%" + tags.get(4) + "%");
                    break;
                case 6:
                    hql = "from com.cpd.soundbook.Entity.Book where (tags like :tag1 or " +
                            "tags like :tag2 or tags like :tag3 or tags like :tag4 or tags like :tag5 or tags like :tag6) and publish = true order by views desc ";
                    query = session.createQuery(hql);
                    query.setParameter("tag1","%" + tags.get(0) + "%");
                    query.setParameter("tag2","%" + tags.get(1) + "%");
                    query.setParameter("tag3","%" + tags.get(2) + "%");
                    query.setParameter("tag4","%" + tags.get(3) + "%");
                    query.setParameter("tag5","%" + tags.get(4) + "%");
                    query.setParameter("tag6","%" + tags.get(5) + "%");
                    break;
                default:
                    break;
            }

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

    @Override
    public void publishBook(int bookid) {
        bookRepository.publishBook(bookid);
    }

    @Override
    public List<Book> findAllBookByViews(int from,int size) {
        Session session = factory.unwrap(org.hibernate.SessionFactory.class).openSession();
        List<Book> books = new ArrayList<>();
        try{
            String hql = "from com.cpd.soundbook.Entity.Book where publish = true order by views desc ";
            Query query = session.createQuery(hql);

            query.setFirstResult(from);
            query.setMaxResults(size);

            books = (List<Book>) query.list();

            session.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return books;
    }
}
