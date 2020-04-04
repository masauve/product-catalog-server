package com.redhat.demo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class ProductEndpointTest {

    @Test
    public void testProductEndpoint() {
        given()
          .when().get("/api/product")
          .then()
             .statusCode(200)
             .body("size()", is(12));
    }

    @Test
    public void testExistingProductEndpoint() {
        given()
          .pathParam("id", 1)
          .when().get("/api/product/{id}")
          .then()
            .statusCode(200)
            .body("id",equalTo(1));
    }

    @Test
    public void testMissingProductEndpoint() {
        given()
          .pathParam("id", 99)
          .when().get("/api/product/{id}")
          .then()
            .statusCode(404);
    }
}