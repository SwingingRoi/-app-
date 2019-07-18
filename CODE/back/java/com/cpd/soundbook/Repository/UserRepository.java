package com.cpd.soundbook.Repository;

import com.cpd.soundbook.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface UserRepository extends JpaRepository<User,Integer> {

    @Query("select u from com.cpd.soundbook.Entity.User u where id=:id")
    User findUserById(@Param("id") int id);

    @Query("select u from com.cpd.soundbook.Entity.User u where account=:account")
    User findUserByAccount(@Param("account") String account);

    @Query("select u from com.cpd.soundbook.Entity.User u where phone=:phone")
    User findUserByPhone(@Param("phone") String phone);

    @Query("select u from com.cpd.soundbook.Entity.User u where email=:email")
    User findUserByEmail(@Param("email") String email);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.User set activated=true where account=:account")
    void activateUser(@Param("account") String account);

    @Modifying
    @Query("update com.cpd.soundbook.Entity.User set account=:account,password=:password,name=:name," +
            "gender=:gender,email=:email,preferences=:preferences where id=:id")
    void modifyInfo(@Param("account") String account,@Param("password") String password,
                    @Param("name") String name,@Param("gender") String gender,@Param("email") String email,
                    @Param("preferences") String preferences,@Param("id") int id);
}
