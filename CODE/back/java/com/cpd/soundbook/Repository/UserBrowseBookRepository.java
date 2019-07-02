package com.cpd.soundbook.Repository;

import com.cpd.soundbook.Entity.UserBrowseBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface UserBrowseBookRepository extends JpaRepository<UserBrowseBook,Integer> {
    @Query("select u from com.cpd.soundbook.Entity.UserBrowseBook u where account=:account and bookid=:bookid" +
            " and time=:time")
    UserBrowseBook findRecord(@Param("account") String account,@Param("bookid") int bookid,@Param("time") String time);

    @Modifying
    @Query("delete from com.cpd.soundbook.Entity.UserBrowseBook u where account=:account")
    void clearRecords(@Param("account") String account);
}
