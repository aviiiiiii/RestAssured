package com.example.api.tests;

import com.example.api.utils.ExcelUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ApiTest {

    @DataProvider(name = "apiData")
    public Object[][] getData() throws Exception {
        String excelPath = "src/test/resources/TestData.xlsx";
        String sheetName = "Sheet1";
        List<Map<String, String>> data = ExcelUtils.readExcelData(excelPath, sheetName);
        Object[][] obj = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            obj[i][0] = data.get(i);
        }
        return obj;
    }

    @Test(dataProvider = "apiData")
    public void apiTests(Map<String, String> row) throws IOException {
        RestAssured.baseURI = "https://reqres.in/api";

        String endpoint = row.get("Endpoint");
        String method = row.get("Method");
        String inputFile = row.get("InputFile");
        int expectedStatus = Integer.parseInt(row.get("ExpectedStatus"));

        var request = RestAssured.given().contentType(ContentType.JSON);
        if (inputFile != null && !inputFile.isBlank()) {
            String payload = Files.readString(Path.of("src/test/resources/" + inputFile));
            request = request.body(payload);
        }
        Response response = request.when().request(method, endpoint);
        Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code mismatch for " + row.get("TestCase"));
    }
}
