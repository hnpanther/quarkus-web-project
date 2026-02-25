package com.hnp.resource;

import com.hnp.testresources.MongoRedisTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(MongoRedisTestResource.class)
public class UserResourceTest {

    @Test
    void shouldCreateUser() {
        given()
                .contentType(ContentType.JSON)
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
}
