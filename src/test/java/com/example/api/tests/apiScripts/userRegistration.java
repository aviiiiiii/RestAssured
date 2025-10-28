package com.example.api.tests.apiScripts;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class userRegistration {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://practice.expandtesting.com";
        RestAssured.basePath = "/notes/api/users";
    }

    @Test(priority = 1)
    public void registerUserSuccess() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Devavikashini",
                        "email", "santhosh0000001@gmail.com",
                        "password", "Password@123"
                ))
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("message", equalTo("User account created successfully"));


    }

    @Test(priority = 2)
    public void registerWithInvalidEmail() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Devavikashini",
                        "email", "santhosh680657.com",
                        "password", "Password@123"
                ))
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("A valid email address is required"));


    }

    @Test(priority = 3)
    public void registerWithDuplicateEmail() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Devavikashini",
                        "email", "santhosh680657@gmail.com",
                        "password", "Password@123"
                ))
                .when()
                .post("/register")
                .then()
                .statusCode(409)
                .body("success", equalTo(false))
                .body("message", equalTo("An account already exists with the same email address"));


    }

    @Test(priority = 4)
    public void registerWithInvalidPassword() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Devavikashini",
                        "email", "santhosh680657@gmail.com",
                        "password", "Pass"
                ))
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Password must be between 6 and 30 characters"));


    }

}
