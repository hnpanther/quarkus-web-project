package com.hnp.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hnp.entity.Role;
import com.hnp.entity.User;
import com.hnp.service.RedisService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class DataInitializer {

    private static final Logger log = Logger.getLogger(DataInitializer.class.getName());

    void onStart(@Observes StartupEvent ev) {

        log.log(Level.INFO, "Data initializer started(onStart)");

        Role adminRole = Role.find("roleName", "ADMIN").firstResult();
        if (adminRole == null) {
            log.log(Level.INFO, "Admin role not found");
            adminRole = new Role();
            adminRole.roleName = "ADMIN";
            adminRole.persist();
            log.log(Level.INFO, "Admin role created");
        } else {
            log.log(Level.INFO, "Admin role found");
        }


        Role userRole = Role.find("roleName", "USER").firstResult();
        if (userRole == null) {
            log.log(Level.INFO, "User role not found");
            userRole = new Role();
            userRole.roleName = "USER";
            userRole.persist();
            log.log(Level.INFO, "User role created");
        } else {
            log.log(Level.INFO, "User role found");
        }

        User adminUser = User.find("username", "admin").firstResult();
        if (adminUser == null) {
            log.log(Level.INFO, "Admin user not found");
            adminUser = new User();
            adminUser.username = "admin";

            String password = "admin";
            adminUser.password = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            adminUser.firstName = "admin";
            adminUser.lastName = "admin";
            adminUser.email = "admin";
            adminUser.roleIds = List.of(adminRole.id);
            adminUser.persist();
            log.log(Level.INFO, "Admin user created");
        } else {
            log.log(Level.INFO, "Admin user found");
            if(adminUser.roleIds == null || !adminUser.roleIds.contains(adminRole.id)) {
                adminUser.roleIds.add(adminRole.id);
                adminUser.update();
                log.log(Level.INFO, "add ADMIN Role to Admin user");
            } else {
                log.log(Level.INFO, "Admin has ADMIN Role");
            }
        }




    }
}
