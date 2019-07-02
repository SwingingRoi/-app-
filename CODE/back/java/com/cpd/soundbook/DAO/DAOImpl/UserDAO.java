package com.cpd.soundbook.DAO.DAOImpl;

import com.cpd.soundbook.Entity.User;
import com.cpd.soundbook.Entity.UserFavBook;
import com.cpd.soundbook.Repository.UserFavBookRepository;
import com.cpd.soundbook.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;


@Repository
public class UserDAO implements com.cpd.soundbook.DAO.DAOInterface.UserDAO {

    @Autowired
    private EntityManagerFactory factory;

    @Autowired
    private UserRepository userRepository;


    @Transactional
    @Override
    public void addUser(User user) {
        try {
            userRepository.save(user);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
    @Override
    public User findUserByAccount(String account) {
        return userRepository.findUserByAccount(account);
    }

    @Transactional
    @Override
    public User findUserByPhone(String phone) {
        return userRepository.findUserByPhone(phone);
    }

    @Transactional
    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Transactional
    @Override
    public User findUserByID(int id) {
        return userRepository.findUserById(id);
    }

    @Transactional
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

    @Transactional
    @Override
    public void updateUser(User user) {
        userRepository.modifyInfo(user.getAccount(),user.getPassword(),user.getName(),user.getGender(),user.getEmail(),user.getId());
    }
}
