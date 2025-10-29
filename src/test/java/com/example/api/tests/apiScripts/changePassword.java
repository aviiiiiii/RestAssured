package com.example.api.tests.apiScripts;

import com.example.api.utils.JsonUtils;
import com.example.api.utils.TestNGXMLGenerator;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class changePassword {

    private static final Logger logger = LoggerFactory.getLogger(changePassword.class);

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://practice.expandtesting.com";
        RestAssured.basePath = "/notes/api/users";
    }

    @Test(dataProvider = "changePasswordData", dataProviderClass = JsonUtils.class)
    private void validateChangePassword(Map<String, String> data) {

        // Login request to fetch token
        var loginResponse = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", data.get("email"),
                        "password", data.get("password")
                ))
                .when()
                .post("/login")
                .then()
                .extract().response();

        JsonPath jsonPath = loginResponse.jsonPath();
        String token = jsonPath.getString("data.token");

        logger.info("Token Received: + token");


        // Change Password request
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("x-auth-token", token)
                .body(Map.of(
                        "currentPassword", data.get("current password"),
                        "newPassword", data.get("new password")
                ))
                .when()
                .post("/change-password")
                .then()
                .statusCode(Integer.parseInt(data.get("status")))
                .body("success", equalTo(Boolean.parseBoolean(data.get("success"))))
                .body("message", equalTo(data.get("Expected Message")));

                logger.info("Validation completed");
    }


    @Test(priority = 1, dataProvider = "changePasswordData", dataProviderClass =  JsonUtils.class)
    public void changePasswordSuccess(Map<String, String> data) {
        if ("Success".equalsIgnoreCase(data.get("Testcases"))) {
            validateChangePassword(data);
        }
    }

    @Test(priority = 2, dataProvider = "changePasswordData", dataProviderClass = JsonUtils.class)
    public void changePasswordInvalidCurrentPassword(Map<String, String> data) {
        if ("Invalid Current Password".equalsIgnoreCase(data.get("Testcases"))) {
            validateChangePassword(data);
        }
    }

    @Test(priority = 3, dataProvider = "changePasswordData", dataProviderClass = JsonUtils.class)
    public void changePasswordIncorrectCurrentPassword(Map<String, String> data) {
        if ("Incorrect Current Password".equalsIgnoreCase(data.get("Testcases"))) {
            validateChangePassword(data);
        }
    }

    @Test(priority = 4, dataProvider = "changePasswordData", dataProviderClass = JsonUtils.class)
    public void changePasswordInvalidNewPassword(Map<String, String> data) {
        if ("Invalid New Password".equalsIgnoreCase(data.get("Testcases"))) {
            validateChangePassword(data);
        }
    }

    @Test(priority = 5, dataProvider = "changePasswordData", dataProviderClass =  JsonUtils.class)
    public void changePasswordCurrentSameAsNew(Map<String, String> data) {
        if ("New Password Same As Current Password".equalsIgnoreCase(data.get("Testcases"))) {
            validateChangePassword(data);
        }
    }
}
