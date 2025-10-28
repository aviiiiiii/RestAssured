package com.example.api.tests.apiScripts;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class loginUser {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://practice.expandtesting.com";
        RestAssured.basePath = "/notes/api/users";
    }

    @Test(priority = 1)
    public void loginUserSuccess() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "santhosh680657@gmail.com",
                        "password", "Password@0987"
                ))
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Login successful"))
                .body("data.token", notNullValue()); // token validation
    }

    @Test(priority = 2)
    public void loginUserWithInvalidEmail() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "santhosh680657.com",
                        "password", "Password@123"
                ))
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("A valid email address is required"));
    }

    @Test(priority = 3)
    public void loginUserWithInvalidPassword() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "santhosh680657@gmail.com",
                        "password", "Pass"
                ))
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Password must be between 6 and 30 characters"));
    }

    @Test(priority = 4)
    public void loginUserWithInvalidCredentials() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "santhosh680657@gmail.com",
                        "password", "Password2345678"
                ))
                .when()
                .post("/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("Incorrect email address or password"));
    }
}
