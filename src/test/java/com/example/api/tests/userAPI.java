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
               .body("success", equalTo(Boolean.parseBoolean(data.get("success"))))
                .body("message", equalTo(data.get("expectedMessage")))
               .extract().response();
//       System.out.println(response.body().prettyPrint());
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


    @Test
    public void changePassword(){

        String token = "9f9c2ea6e5a74fc3b5f5167be561dab9d28f2bdf32ea48d4892f68b9924ef322";

        var response =  given().contentType(ContentType.URLENC)
                .accept(ContentType.JSON)
                .header("x-auth-token",token)
                .formParam("currentPassword","test@1234")
                .formParam("newPassword","test@12345")
                .when()
                .post("/users/change-password")
                .then()
//                .statusCode(200)
//                .body("success",equalTo(true))
//                .body("message",equalTo("The password was successfully updated"))
                .extract()
                .response();

        System.out.println(response.body().prettyPrint());

    }




}
