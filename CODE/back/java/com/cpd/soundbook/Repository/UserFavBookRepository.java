package com.cpd.soundbook.Repository;

import com.cpd.soundbook.Entity.UserFavBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface UserFavBookRepository extends JpaRepository<UserFavBook,Integer> {

    @Modifying
    @Query("delete from com.cpd.soundbook.Entity.UserFavBook where account=:account and bookid=:bookid")
    void cancelFav(@Param("account") String account,@Param("bookid") int bookid);

    @Query("select u from com.cpd.soundbook.Entity.UserFavBook u where account=:account and bookid=:bookid")
    UserFavBook findByAccountAndBookid(@Param("account") String account,@Param("bookid") int bookid);
}
