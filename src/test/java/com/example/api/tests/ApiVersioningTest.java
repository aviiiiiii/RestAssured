    package com.example.api.tests;

    import com.example.api.utils.ExcelUtils;
    import io.qameta.allure.*;
    import io.restassured.RestAssured;
    import io.restassured.http.ContentType;
    import io.restassured.path.json.JsonPath;
    import io.restassured.response.Response;
    import org.testng.Assert;
    import org.testng.annotations.*;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.stream.Collectors;

    @Epic("API Versoning Tests")
    @Feature("GET  and POST API Validation")
    public class ApiVersioningTest {

        private Set<String> targetVersions;


        @Parameters("version")
        @BeforeTest
        @Step("Initialize target API versions")
        public void initVersion(String version) {
            // Split by comma to allow multiple versions like "v1,v2"
            targetVersions = Arrays.stream(version.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            Allure.step("Target versions initialized: " + targetVersions);
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

        }

        @Test(dataProvider = "getApiData", description = "Verify GET API responses for each version")
        @Severity(SeverityLevel.CRITICAL)
        @Story("GET API Versioning Test")
        @Description("Validates GET endpoints for multiple API versions using data from Excel.")
        public void getMethods(Map<String, String> row){
            RestAssured.baseURI = "http://localhost:5051";


            String endpoint = row.get("Endpoint");
            String method = row.get("Method");
            String version = row.get("Version");
            String outputData = row.get("OutputData");
            int expectedStatus = Integer.parseInt(row.get("ExpectedStatus"));

            Allure.parameter("Version", version);
            Allure.parameter("Endpoint", endpoint);
            Allure.parameter("TestCase", row.get("TestCase"));

            Allure.step("Sending GET request to " + version + endpoint);
            Response response = RestAssured.given()
                    .when()
                    .request(method, version + endpoint)
                    .then()
                    .extract()
                    .response();

            Allure.step("Validating response status and data");
            Allure.addAttachment("Response Body", "application/json", response.getBody().asPrettyString());

            Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code mismatch for " + row.get("TestCase"));
            JsonPath jsonPath = response.jsonPath();
            String out = jsonPath.getString("data");
            Assert.assertEquals(out, outputData, "Output data mismatch for " + row.get("TestCase"));
        }



        @Test(dataProvider = "postApiData", description = "Verify POST API responses for each version")
        @Severity(SeverityLevel.BLOCKER)
        @Story("POST API Versioning Test")
        @Description("Validates POST endpoints for multiple API versions using data from Excel.")
        public void postMethods(Map<String, String> row) throws IOException {
            RestAssured.baseURI = "http://localhost:5051";

            String endpoint = row.get("Endpoint");
            String method = row.get("Method");
            String version = row.get("Version");
            String outputData = row.get("OutputData");
            int expectedStatus = Integer.parseInt(row.get("ExpectedStatus"));


            Allure.parameter("Version", version);
            Allure.parameter("Endpoint", endpoint);
            Allure.parameter("TestCase", row.get("TestCase"));

            var request = RestAssured.given().contentType(ContentType.JSON);

            String inputFile = row.get("InputFile");
            if (inputFile != null && !inputFile.isBlank()) {
                String payloadPath = "src/test/resources/payloads/" + inputFile;
                String payload = Files.readString(Path.of(payloadPath));
                Allure.addAttachment("Request Payload", "application/json", payload);
                request = request.body(payload);
            }

            Allure.step("Sending POST request to " + version + endpoint);
            Response response = request.when()
                    .request(method, version + endpoint)
                    .then()
                    .extract()
                    .response();

            Allure.step("Validating response");
            Allure.addAttachment("Response Body", "application/json", response.getBody().asPrettyString());

            Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code mismatch for " + row.get("TestCase"));
            JsonPath jsonPath = response.jsonPath();
            String out = jsonPath.getString("data");
            Assert.assertEquals(out, outputData, "Output data mismatch for " + row.get("TestCase"));
        }
    }
