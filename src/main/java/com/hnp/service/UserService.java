package com.hnp.service;

import com.hnp.entity.User;
import com.hnp.repository.UserRepository;
import com.hnp.resource.UserResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class.getName());

    @Inject
    UserRepository userRepository;


    public User createUser(User user) {
        return userRepository.create(user);
    }


    public Optional<User> findById(String id) {
        return userRepository.findUserById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAllUsers();
    }

    public List<User> listUsersPaginated(int page, int size) {
        return userRepository.listPaginated(page, size);
    }

    public List<User> searchUsers(String searchTerm, int page, int size) {
        return userRepository.search(searchTerm, page, size);
    }

    public long countAllUsers() {
        return userRepository.countAllUsers();
    }

    public User updateUser(User user) {
        return userRepository.updateUser(user);
    }

    public boolean deleteUser(String id) {
        return userRepository.deleteUserById(id);
    }


}
