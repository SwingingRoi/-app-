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
    @Query("update com.cpd.soundbook.Entity.Book set name=:name,intro=:intro,surface=:surface where id=:id")
    void modifyInfo(@Param("name") String name,@Param("intro") String intro,@Param("surface") String surface,
                    @Param("id") int id);
}
