package com.example.api.tests.apiScripts;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class changePassword {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://practice.expandtesting.com";
        RestAssured.basePath = "/notes/api/users";
    }


    @Test(priority = 1)
    public void changePasswordSuccess(){

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email","santhosh680657@gmail.com" ,
                        "password", "Password@0987"
                ))
                .when()
                .post("/login")
                .then()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String token = jsonPath.getString("data.token");

        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("x-auth-token",token)
                .body(Map.of(
                        "currentPassword","Password@0987",
                        "newPassword","Password@3245"
                ))
                .when()
                .post("/change-password")
                .then()
                .statusCode(200)
                .body("success",equalTo(true))
                .body("message",equalTo("The password was successfully updated"))
                .extract()
                .response();


    }

    @Test(priority = 2)
    public void changePasswordInvalidCurrentPassword(){

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email","santhosh680657@gmail.com" ,
                        "password", "Password@0987"
                ))
                .when()
                .post("/login")
                .then()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String token = jsonPath.getString("data.token");

        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("x-auth-token",token)
                .body(Map.of(
                        "currentPassword","Pass",
                        "newPassword","Password@3245"
                ))
                .when()
                .post("/change-password")
                .then()
                .statusCode(400)
                .body("success",equalTo(false))
                .body("message",equalTo("Current password must be between 6 and 30 characters"))
                .extract()
                .response();


    }

    @Test(priority = 3)
    public void changePasswordIncorrectPassword(){

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email","santhosh680657@gmail.com" ,
                        "password", "Password@0987"
                ))
                .when()
                .post("/login")
                .then()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String token = jsonPath.getString("data.token");

        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("x-auth-token",token)
                .body(Map.of(
                        "currentPassword","Pass@11100",
                        "newPassword","Password@3245"
                ))
                .when()
                .post("/change-password")
                .then()
                .statusCode(400)
                .body("success",equalTo(false))
                .body("message",equalTo("The current password is incorrect"))
                .extract()
                .response();


    }

    @Test(priority = 4)
    public void changePasswordInvalidNewPassword(){

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email","santhosh680657@gmail.com" ,
                        "password", "Password@0987"
                ))
                .when()
                .post("/login")
                .then()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String token = jsonPath.getString("data.token");

        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("x-auth-token",token)
                .body(Map.of(
                        "currentPassword","Password@0987",
                        "newPassword","Pass"
                ))
                .when()
                .post("/change-password")
                .then()
                .statusCode(400)
                .body("success",equalTo(false))
                .body("message",equalTo("New password must be between 6 and 30 characters"))
                .extract()
                .response();


    }

    @Test(priority = 5)
    public void currentPasswordSameAsNewPassword(){

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email","santhosh680657@gmail.com" ,
                        "password", "Password@0987"
                ))
                .when()
                .post("/login")
                .then()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String token = jsonPath.getString("data.token");

        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("x-auth-token",token)
                .body(Map.of(
                        "currentPassword","Password@0987",
                        "newPassword","Password@0987"
                ))
                .when()
                .post("/change-password")
                .then()
                .statusCode(400)
                .body("success",equalTo(false))
                .body("message",equalTo("The new password should be different from the current password"))
                .extract()
                .response();


    }
}
