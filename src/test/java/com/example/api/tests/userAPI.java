package com.example.api.tests;


import com.example.api.utils.ExcelUtilsUsersAPI;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class userAPI {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://practice.expandtesting.com/notes/api";
    }

    @Test(dataProvider = "registerData", dataProviderClass = ExcelUtilsUsersAPI.class)
    public void registerUser(Map<String, String> data) {

        System.out.println(data.get("status"));

       var response= given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", data.get("name"),
                        "email", data.get("email"),
                        "password", data.get("password")
                ))
                .when()
                .post("/users/register")
                .then()
                .statusCode(Integer.parseInt(data.get("status")))
               .body("success", equalTo(Boolean.parseBoolean(data.get("expectedSuccess"))))
                .body("message", equalTo(data.get("expectedMessage")));
       System.out.println(response);
    }

    @Test
    public  void loginUser() throws IOException{

       given().contentType(ContentType.JSON).body(Map.of(
           "email","aviii134@gmail.com",
           "password","password123"
        )).when().post("users/login").then()
                .statusCode(200)
                .body("success",equalTo(true))
                .body("data.token",notNullValue())
                .body("message",equalTo("Login successful"))
                .extract()
                .response();

    }





}
