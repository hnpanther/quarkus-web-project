package com.hnp.repository;

import com.hnp.entity.User;
import com.hnp.service.UserService;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepository<User> {

    private static final Logger log = Logger.getLogger(UserService.class.getName());

    public User create(User user) {
        persist(user);
        return user;
    }

    public Optional<User> findUserById(String id) {
        log.log(Level.INFO, "findUserById=" + id);
        return find("_id", toObjectId(id)).firstResultOptional();
    }

    public Optional<User> findUserByUsername(String username) {
        log.log(Level.INFO, "findUserByUsername=" + username);
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findUserByEmail(String email) {
        log.log(Level.INFO, "findUserByEmail=" + email);
        return find("email", email).firstResultOptional();
    }

    public User updateUser(User user) {
        log.log(Level.INFO, "updateUser=" + user);
        persist(user);
        return user;
    }

    public boolean deleteUserById(String id) {
        log.log(Level.INFO, "deleteUserById=" + id);
        return delete("_id", toObjectId(id)) > 0;
    }

    public List<User> findAllUsers() {
        log.log(Level.INFO, "findAllUsers");
        return findAll().list();
    }

    public List<User> listPaginated(int page, int size) {
        log.log(Level.INFO, "listPaginated, page=" + page + ", size=" + size);
        return findAll().page(page, size).list();
    }

    public List<User> search(String search, int page, int size) {
        log.log(Level.INFO, "search, page=" + page + ", size=" + size + ", search=" + search);
        String query = "username like ?1 or email like ?1";
        String param = ".*" + search + ".*";
        return find(query, param, page, size).list();
    }


    public long countAllUsers() {
        log.log(Level.INFO, "countAllUsers");
        return count();
    }





    public ObjectId toObjectId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Id cannot be null or empty");
        }

        if (!ObjectId.isValid(id)) {
            throw new IllegalArgumentException("Invalid ObjectId format: " + id);
        }

        return new ObjectId(id);
    }

}
