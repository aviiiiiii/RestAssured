package com.example.api.tests;

import com.example.api.utils.ExcelUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ApiVersioningTest {

    private Set<String> targetVersions;

    @Parameters("version")
    @BeforeTest
    public void initVersion(String version) {
        // Split by comma to allow multiple versions like "v1,v2"
        targetVersions = Arrays.stream(version.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    @DataProvider(name = "getApiData")
    public Object[][] getData() throws Exception {
        String excelPath = "src/test/resources/SampleTestData.xlsx";
        String sheetName = "GET";
        List<Map<String, String>> data = ExcelUtils.readExcelData(excelPath, sheetName);

        return data.stream()
                .filter(row -> targetVersions.contains(row.get("Version")))
                .map(row -> new Object[]{row})
                .toArray(Object[][]::new);
    }

    @DataProvider(name = "postApiData")
    public Object[][] getData_2() throws Exception {
        String excelPath = "src/test/resources/SampleTestData.xlsx";
        String sheetName = "POST";
        List<Map<String, String>> data = ExcelUtils.readExcelData(excelPath, sheetName);

        return data.stream()
                .filter(row -> targetVersions.contains(row.get("Version")))
                .map(row -> new Object[]{row})
                .toArray(Object[][]::new);


        //        Object[][] obj = new Object[data.size()][1];
//        for (int i = 0; i < data.size(); i++) {
//            obj[i][0] = data.get(i);
//        }
//        return obj;
    }

    @Test(dataProvider = "getApiData")
    public void getMethods(Map<String, String> row){
        RestAssured.baseURI = "http://localhost:5051";

        String endpoint = row.get("Endpoint");
        String method = row.get("Method");
        String version = row.get("Version");
        String outputData = row.get("OutputData");
        int expectedStatus = Integer.parseInt(row.get("ExpectedStatus"));

        Response response = RestAssured.given().when().request(method, version+endpoint).then().extract().response();
        Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code mismatch for " + row.get("TestCase"));
        JsonPath jsonPath = response.jsonPath();
        String out = jsonPath.getString("data");
        Assert.assertEquals(out, outputData, "Output data mismatch for " + row.get("TestCase"));
        System.out.println(response.body().asString());
    }



    @Test(dataProvider = "postApiData")
    public void postMethods(Map<String, String> row) throws IOException {
        RestAssured.baseURI = "http://localhost:5051";

        String endpoint = row.get("Endpoint");
        String method = row.get("Method");
        String version = row.get("Version");
        String outputData = row.get("OutputData");
        int expectedStatus = Integer.parseInt(row.get("ExpectedStatus"));


        var request = RestAssured.given().contentType(ContentType.JSON);

        String inputFile = row.get("InputFile");
        if (inputFile != null && !inputFile.isBlank()) {
            String payload = Files.readString(Path.of("src/test/resources/payloads/" + inputFile));
            request = request.body(payload);
        }


        Response response = request.when().request(method, version+endpoint).then().extract().response();
        Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code mismatch for " + row.get("TestCase"));
        JsonPath jsonPath = response.jsonPath();
        String out = jsonPath.getString("data");
        Assert.assertEquals(out, outputData, "Output data mismatch for " + row.get("TestCase"));
        System.out.println(response.body().asString());
    }
}
