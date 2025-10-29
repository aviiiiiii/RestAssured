package com.example.api.tests.apiScripts;

import com.example.api.utils.JsonUtils;
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
    @Test(dataProvider = "loginData", dataProviderClass = JsonUtils.class)
    private void validateLogin(Map<String, String> data) {
        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", data.get("email"),
                        "password", data.get("password")
                ))
                .when()
                .post("/login")
                .then()
                .statusCode(Integer.parseInt(data.get("status")))
                .body("success", equalTo(Boolean.parseBoolean(data.get("success"))))
                .body("message", equalTo(data.get("Expected Message")));

        if (Boolean.parseBoolean(data.get("success"))) {
            response.body("data.token", notNullValue());
            System.out.println("Token validation successful");
        } else {
            System.out.println("Login failed, token not provided");
        }
    }

    @Test(priority = 1, dataProvider = "loginData", dataProviderClass = JsonUtils.class)
    public void loginUserSuccess(Map<String, String> data) {
        if ("Success".equalsIgnoreCase(data.get("Testcases"))) {
            validateLogin(data);
        }
    }

    @Test(priority = 2, dataProvider = "loginData", dataProviderClass = JsonUtils.class)
    public void loginUserWithInvalidEmail(Map<String, String> data) {
        if ("Invalid Email".equalsIgnoreCase(data.get("Testcases"))) {
            validateLogin(data);
        }
    }

    @Test(priority = 3, dataProvider = "loginData", dataProviderClass = JsonUtils.class)
    public void loginUserWithInvalidPassword(Map<String, String> data) {
        if ("Invalid Password".equalsIgnoreCase(data.get("Testcases"))) {
            validateLogin(data);
        }
    }


    @Test(priority = 4, dataProvider = "loginData", dataProviderClass = JsonUtils.class)
    public void loginUserWithInvalidCredentials(Map<String, String> data) {
        if ("Incorrect Password".equalsIgnoreCase(data.get("Testcases"))) {
            validateLogin(data);
        }
    }

}
