package com.cpd.soundbook.Repository;

import com.cpd.soundbook.Entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface DraftRepository extends JpaRepository<Draft,Integer> {
    @Query("select d from com.cpd.soundbook.Entity.Draft d where d.bookid=:bookid")
    Draft findDraftByBookid(@Param("bookid") int bookid);

    @Modifying
    @Query("delete from com.cpd.soundbook.Entity.Draft d where d.bookid=:bookid")
    void deleteByBookid(@Param("bookid") int bookid);
}
