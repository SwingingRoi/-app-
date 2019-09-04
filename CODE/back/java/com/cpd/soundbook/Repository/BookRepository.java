package com.cpd.soundbook.Repository;

import com.cpd.soundbook.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface BookRepository extends JpaRepository<Book,Integer> {

    @Query("select b from com.cpd.soundbook.Entity.Book b where id=:id")
    Book findBookById(@Param("id") int id);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.Book set name=:name,intro=:intro,tags=:tags,surface=:surface where id=:id")
    void modifyInfo(@Param("name") String name,@Param("intro") String intro,@Param("surface") String surface,
                    @Param("tags") String tags,@Param("id") int id);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.Book set chapters=chapters + 1 where id=:id")
    void increaChapter(@Param("id") int bookid);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.Book set chapters=chapters - :number where id=:id")
    void decreaChapter(@Param("number") int number,@Param("id") int bookid);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.Book set views = views + 1 where id=:id")
    void increaView(@Param("id") int bookid);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.Book set publish = true where id=:id")
    void publishBook(@Param("id") int bookid);
}
