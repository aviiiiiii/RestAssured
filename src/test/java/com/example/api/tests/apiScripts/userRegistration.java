package com.example.api.tests.apiScripts;

import com.example.api.utils.JsonUtils;
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

    @Test(dataProvider = "registerData", dataProviderClass = JsonUtils.class)
    private void validateUserRegistration(Map<String, String> data) {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", data.get("name"),
                        "email", data.get("email"),
                        "password", data.get("password")
                ))
                .when()
                .post("/register")
                .then()
                .statusCode(Integer.parseInt(data.get("status")))
                .body("success", equalTo(Boolean.parseBoolean(data.get("success"))))
                .body("message", equalTo(data.get("Expected Message")));
    }

    @Test(priority = 1, dataProvider = "registerData", dataProviderClass = JsonUtils.class)
    public void registerUserSuccess(Map<String, String> data) {
        if ("Success".equals(data.get("Testcases"))) {
            validateUserRegistration(data);
        }
    }


    @Test(priority = 2, dataProvider = "registerData", dataProviderClass = JsonUtils.class)
    public void registerWithInvalidEmail(Map<String, String> data) {
        if ("Invalid Email".equals(data.get("Testcases"))) {
            validateUserRegistration(data);
        }
    }


    @Test(priority = 3, dataProvider = "registerData", dataProviderClass = JsonUtils.class)
    public void registerWithDuplicateEmail(Map<String, String> data) {
        if ("Duplicate Email".equals(data.get("Testcases"))) {
            validateUserRegistration(data);
        }
    }

    @Test(priority = 4, dataProvider = "registerData", dataProviderClass = JsonUtils.class)
    public void registerWithInvalidPassword(Map<String, String> data) {
        if ("Invalid Password".equals(data.get("Testcases"))) {
            validateUserRegistration(data);
        }
    }

}
