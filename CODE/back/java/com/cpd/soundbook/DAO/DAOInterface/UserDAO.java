package com.cpd.soundbook.DAO.DAOInterface;

import com.cpd.soundbook.Entity.User;
import org.springframework.stereotype.Component;

@Component(value = "userDAO")
public interface UserDAO {
    void addUser(User user);

    User findUserByAccount(String account);

    User findUserByPhone(String phone);

    User findUserByEmail(String email);

    User findUserByID(int id);

    boolean activateUser(String account);

    void updateUser(User user);
}
