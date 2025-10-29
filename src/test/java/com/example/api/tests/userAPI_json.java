package com.example.api.tests;

import com.example.api.utils.JsonUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class userAPI_json {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://practice.expandtesting.com/notes/api";
    }

    @Test(dataProvider = "registerData", dataProviderClass = JsonUtils.class)
    public void registerUser(Map<String, String> data) {

        given()
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
                .body("message", equalTo(data.get("Expected Message")))
               .extract().response();

    }

    @Test(dataProvider = "loginData",dataProviderClass = JsonUtils.class)
    public void loginUser(Map<String, String> data) {

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", data.get("email"),
                        "password", data.get("password")
                ))
                .when()
                .post("users/login")
                .then()
                .statusCode(Integer.parseInt(data.get("status")))
                .body("success", equalTo(Boolean.parseBoolean(data.get("success"))))
                .body("message", equalTo(data.get("Expected Message")));

        if (Boolean.parseBoolean(data.get("success"))) {
            response.body("data.token", notNullValue());
            System.out.println("Token validated");
        }else{
            System.out.println("Error occurred so token doesn't exist ");
        }

    }

    @Test(dataProvider = "changePasswordData",dataProviderClass = JsonUtils.class)
    public void changePassword(Map<String, String> data){

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", data.get("email"),
                        "password", data.get("password")
                ))
                .when()
                .post("users/login")
                .then()
                .extract().response();
        System.out.println(response.body().prettyPrint());
        JsonPath jsonPath = response.jsonPath();
        String token = jsonPath.getString("data.token");

        given().contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("x-auth-token",token)
                .body(Map.of(
                        "currentPassword",data.get("current password"),
                        "newPassword",data.get("new password")
                ))
                .when()
                .post("/users/change-password")
                .then()
                .statusCode(Integer.parseInt(data.get("status")))
                .body("success",equalTo(Boolean.parseBoolean(data.get("success"))))
                .body("message",equalTo(data.get("Expected Message")))
                .extract()
                .response();


    }

}
