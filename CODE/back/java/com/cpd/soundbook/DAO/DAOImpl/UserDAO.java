package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.User;
import com.cpd.soundbook.Entity.UserFavBook;
import com.cpd.soundbook.Repository.UserFavBookRepository;
import com.cpd.soundbook.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;


@Repository
public class UserDAO implements com.cpd.soundbook.DAO.DAOInterface.UserDAO {

    @Autowired
    private EntityManagerFactory factory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFavBookRepository userFavBookRepository;

    @Override
    public void addUser(User user) {
        try {
            userRepository.save(user);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public User findUserByAccount(String account) {
        return userRepository.findUserByAccount(account);
    }

    @Override
    public User findUserByPhone(String phone) {
        return userRepository.findUserByPhone(phone);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User findUserByID(int id) {
        return userRepository.findUserById(id);
    }

    @Override
    public boolean activateUser(String account) {
        try {
            userRepository.activateUser(account);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void updateUser(User user) {
        userRepository.modifyInfo(user.getAccount(),user.getPassword(),user.getName(),user.getGender(),user.getEmail(),user.getId());
    }
}
