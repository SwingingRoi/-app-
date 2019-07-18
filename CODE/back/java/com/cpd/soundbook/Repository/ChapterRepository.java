package com.cpd.soundbook.Repository;

import com.cpd.soundbook.Entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface ChapterRepository extends JpaRepository<Chapter,Integer> {

    @Query("select c from com.cpd.soundbook.Entity.Chapter c where c.id=:id")
    Chapter findChapterById(@Param("id") int id);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.Chapter set title=:title,content=:content," +
            " speechpath=:speechpath,time=:time,bgmpath=:bgmpath  where id=:id")
    void modifyChapter(@Param("id") int id,@Param("title") String title,@Param("content") String content,
                       @Param("speechpath") String speechpath,@Param("time") String length,
                       @Param("bgmpath") String bgmPath);

    @Query("select count(id) from com.cpd.soundbook.Entity.Chapter where bookid=:bookid")
    int getChapterNumbers(@Param("bookid") int bookid);
}
