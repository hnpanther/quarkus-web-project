package com.hnp.integration.resource;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hnp.entity.Role;
import com.hnp.entity.User;
import com.hnp.integration.testresources.MongoRedisTestResource;
import com.hnp.security.AuthResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(MongoRedisTestResource.class)
public class UserResourceTest {


    String adminToken;

    @BeforeEach
    void setup() {

        User.deleteAll();
        Role.deleteAll();

        Role adminRole = new Role();
        adminRole.roleName = "ADMIN";
        adminRole.persist();

        User admin = new User();
        admin.username = "admin";
        admin.password = BCrypt.withDefaults().hashToString(12, "admin".toCharArray());
        admin.roleIds = List.of(adminRole.id);
        admin.persist();

        AuthResource.LoginRequest loginRequest = new AuthResource.LoginRequest();
        loginRequest.username = admin.username;
        loginRequest.password = admin.password;

        adminToken =
                given()
                        .contentType(ContentType.JSON)
//                        .body(loginRequest)
                        .body("""
                        {
                                          "password": "admin",
                                          "username": "admin"
                                        }
                        """)
                        .when()
                        .post("/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("token");


    }

    @Test
    void shouldCreateUser() {
        given()
                .contentType(ContentType.JSON)
//                .auth().oauth2(adminToken)
                .header("Authorization", "Bearer " + adminToken)
                .body("""

                        {
                  "email": "hnp",
                  "firstName": "hnp",
                  "lastName": "hnp",
                  "password": "hnp",
                  "username": "hnp"
                }
                """)
        .when()
                .post("/users")
        .then()
                .statusCode(201);
    }



    @Test
    void shouldGetUser() {
        User user = new User();
        user.username = "user2";
        user.password = "user2";
        user.persist();

        given()
                .auth().oauth2(adminToken)
                .when()
                .get("/users/" + user.id)
                .then()
                .statusCode(200)
                .body("username", equalTo("user2"));
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.username = "user2";
        user.password = "user2";
        user.persist();


        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "username": "user3",
                      "password": "user2"
                    }
                    """)
                .when()
                .put("/users/" + user.id)
                .then()
                .statusCode(200)
                .body("username", equalTo("user3"));

    }


    @Test
    void shouldDeleteUser() {
        User user = new User();
        user.username = "user2";
        user.password = "user2";
        user.persist();

        given()
                .auth().oauth2(adminToken)
                .when()
                .delete("/users/" + user.id)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(adminToken)
                .when()
                .get("/users/" + user.id)
                .then()
                .statusCode(404);
    }



    @Test
    void shouldFailLoginWithWrongPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "username": "admin",
                      "password": "wrong"
                    }
            """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }


}
