package com.redhat.demo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class CategoryEndpointTest {

    @Test
    public void testCategoryEndpoint() {
        given()
          .when().get("/api/category")
          .then()
             .statusCode(200)
             .body("size()", is(3));
    }

    @Test
    public void testExistingCategoryEndpoint() {
        given()
          .pathParam("id", 1)
          .when().get("/api/category/{id}")
          .then()
            .statusCode(200)
            .body("id",equalTo(1));
    }

    @Test
    public void testMissingCategoryEndpoint() {
        given()
          .pathParam("id", 99)
          .when().get("/api/category/{id}")
          .then()
            .statusCode(404);
    }
}