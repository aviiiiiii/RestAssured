package com.example.api.utils;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.xml.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;



public class TestNGXMLGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TestNGXMLGenerator.class);
    static String appName = ConfigReader.getProperty("appName");
    static String[] suiteArray = appName.split("_");
    static String appModule = suiteArray[0];
    static String suiteType = suiteArray[1];
    private final String suiteName;
    private final String testName;
    private final String className;
    private final String testCaseID;
    private final String methodName;
    private final String[] paramName;
    private final String[] paramValue;
    private final String executeFlag;
    public static String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    public static String resultsDir = "allure-reports/allure-results";

    private TestNGXMLGenerator(Builder builder){
        suiteName = builder.suiteName;
        testName = builder.testName;
        className = builder.className;
        testCaseID = builder.testCaseID;
        methodName = builder.methodName;
        paramName = builder.paramName;
        paramValue = builder.paramValue;
        executeFlag = builder.executeFlag;
    }

    public static class Builder {
        private String suiteName;
        private String testName;
        private String className;
        private String testCaseID;
        private String methodName;
        private String[] paramName;
        private String[] paramValue;
        private String executeFlag;

        /*
         * Method Description : Sets suite name for builder
         * Input Parameter(s) if any : suiteName (String)
         * Output Parameter(s) if any : Builder
         */
        public Builder setSuiteName(String suiteName){ this.suiteName = suiteName; return this; }

        /*
         * Method Description : Sets test name for builder
         * Input Parameter(s) if any : testName (String)
         * Output Parameter(s) if any : Builder
         */
        public Builder setTestName(String testName){ this.testName = testName; return this; }

        /*
         * Method Description : Sets class name for builder
         * Input Parameter(s) if any : className (String)
         * Output Parameter(s) if any : Builder
         */
        public Builder setClassName(String className){ this.className = className; return this; }

        /*
         * Method Description : Sets test case ID
         * Input Parameter(s) if any : testCaseID (String)
         * Output Parameter(s) if any : Builder
         */
        public Builder setTestCaseID(String testCaseID){ this.testCaseID = testCaseID; return this; }

        /*
         * Method Description : Sets method name to include in XML
         * Input Parameter(s) if any : methodName (String)
         * Output Parameter(s) if any : Builder
         */
        public Builder setMethodName(String methodName){ this.methodName = methodName; return this; }

        /*
         * Method Description : Sets parameter names
         * Input Parameter(s) if any : paramName (String[])
         * Output Parameter(s) if any : Builder
         */
        public Builder setParamName(String[] paramName){ this.paramName = paramName; return this; }

        /*
         * Method Description : Sets parameter values
         * Input Parameter(s) if any : paramValue (String[])
         * Output Parameter(s) if any : Builder
         */
        public Builder setParamValue(String[] paramValue){ this.paramValue = paramValue; return this; }

        /*
         * Method Description : Sets execute flag
         * Input Parameter(s) if any : executeFlag (String)
         * Output Parameter(s) if any : Builder
         */
        public Builder setExecuteFlag(String executeFlag){ this.executeFlag = executeFlag; return this; }

        /*
         * Method Description : Builds the TestNGXMLGenerator instance
         * Input Parameter(s) if any : None
         * Output Parameter(s) if any : TestNGXMLGenerator
         */
        public TestNGXMLGenerator build() { return new TestNGXMLGenerator(this); }

    }

    public String getTestCaseID() {
        return this.testCaseID;
    }

    /*
     * Method Description : Main entry to read Excel, generate XML, and run suite
     * Input Parameter(s) if any : args (String[])
     * Output Parameter(s) if any : void
     */
    public static void main(String[] args) {
        String outputPath = "generated.xml";
        String path = ConfigReader.getProperty("testNGPath");
        List<TestNGXMLGenerator> testData = readExcel(path);
        System.setProperty("allure.results.directory", resultsDir);
        XmlSuite suite = buildSuite(testData);
        writeSuiteToXmlFile(suite, outputPath);
//        runSuite(suite);
    }

    /*
     * Method Description : Reads test configuration from Excel sheet
     * Input Parameter(s) if any : path (String) - Excel file path
     * Output Parameter(s) if any : List<TestNGXMLGenerator>
     */
    static List<TestNGXMLGenerator> readExcel(String path) {
        List<TestNGXMLGenerator> data = new ArrayList<>();
        try(FileInputStream fis = new FileInputStream(path)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(appModule);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                data.add(new TestNGXMLGenerator.Builder()
                        .setSuiteName(row.getCell(0).getStringCellValue())
                        .setTestName(row.getCell(1).getStringCellValue())
                        .setClassName(row.getCell(2).getStringCellValue())
                        .setTestCaseID(row.getCell(3).getStringCellValue())
                        .setMethodName(row.getCell(4).getStringCellValue())
                        .setParamName(row.getCell(5).getStringCellValue().split(","))
                        .setParamValue(row.getCell(6).getStringCellValue().split(","))
                        .setExecuteFlag(row.getCell(7).getStringCellValue()).build());
            }
            workbook.close();
        } catch (IOException e) {
            logger.error("Exception occurred while reading excel", e);
        }
        return data;
    }

    /*
     * Method Description : Builds XmlSuite based on test data
     * Input Parameter(s) if any : data (List<TestNGXMLGenerator>)
     * Output Parameter(s) if any : XmlSuite
     */
    static XmlSuite buildSuite(List<TestNGXMLGenerator> data) {
        XmlSuite suite = new XmlSuite();
        suite.setName(appModule + " " + suiteType + " Suite");
        suite.addListener("framework.listeners.CustomTestListener");
        suite.addListener("framework.listeners.DynamicDependencyTransformer");
        suite.addListener("io.qameta.allure.testng.AllureTestNg");

        Map<String, Map<String, List<String>>> testMap = new LinkedHashMap<>();
        Map<String, Map<String, String>> testParams = new LinkedHashMap<>();

        for (TestNGXMLGenerator row : data) {
            if (!"Y".equalsIgnoreCase(row.executeFlag) || !row.suiteName.contains(suiteType)) continue;

            testMap.computeIfAbsent(row.testName, k -> new LinkedHashMap<>())
                    .computeIfAbsent(row.className, k -> new ArrayList<>())
                    .add(row.methodName);

            Map<String, String> params = testParams.getOrDefault(row.testName, new LinkedHashMap<>());
            for (int i = 0; i < row.paramName.length; i++) {
                params.put(row.paramName[i], row.paramValue[i]);
            }
            params.put("testCaseID", row.getTestCaseID());
            testParams.put(row.testName, params);
        }

        //setSuiteParameters(suite, testParams);
        configureParallelism(suite, testMap.size());

        for (String testName : testMap.keySet()) {
            XmlTest xmlTest = new XmlTest(suite);
            xmlTest.setName(testName);

            Map<String, String> parameters = testParams.get(testName);
            xmlTest.setParameters(parameters);

            List<XmlClass> xmlClasses = new ArrayList<>();
            for (Map.Entry<String, List<String>> classEntry : testMap.get(testName).entrySet()) {
                XmlClass xmlClass = new XmlClass(classEntry.getKey());
                List<XmlInclude> includes = new ArrayList<>();
                for (String method : classEntry.getValue()) {
                    includes.add(new XmlInclude(method));
                }
                xmlClass.setIncludedMethods(includes);
                xmlClasses.add(xmlClass);
            }
            xmlTest.setXmlClasses(xmlClasses);
        }
        return suite;
    }

    /*
     * Method Description : Sets suite-level parameters based on test params
     * Input Parameter(s) if any : suite (XmlSuite), testParams (Map<String, Map<String,String>>)
     * Output Parameter(s) if any : none
     */
//    private static void setSuiteParameters(XmlSuite suite, Map<String, Map<String, String>> testParams) {
//        String env = getRunTimeVariables("env");
//        if (env == null || env.isEmpty()) {
//            throw new RuntimeException("Suite-level parameter 'env' is missing. Please provide it in runtime variables.");
//        }
//        suite.setParameters(Collections.singletonMap("env", env));
//    }

    /*
     * Method Description : Configures suite parallelism
     * Input Parameter(s) if any : suite (XmlSuite), testCount (int)
     * Output Parameter(s) if any : none
     */
    private static void configureParallelism(XmlSuite suite, int testCount) {
        String flag = ConfigReader.getProperty("parallelFlag");
        int threadCount = Integer.parseInt(ConfigReader.getProperty("threadCount"));

        if ("true".equalsIgnoreCase(flag)) {
            suite.setParallel(XmlSuite.ParallelMode.TESTS);
            suite.setThreadCount(Math.min(testCount, threadCount));
            logger.info("Parallel execution enabled. Mode=TESTS, ThreadCount={}", suite.getThreadCount());
        } else if ("false".equalsIgnoreCase(flag)) {
            suite.setThreadCount(1);
            logger.info("Parallel execution disabled. Running sequentially with ThreadCount=1");
        } else {
            logger.warn("Invalid parallelFlag value: {}. Defaulting to sequential execution.", flag);
            suite.setThreadCount(1);
        }
    }

    /*
     * Method Description : Runs the generated TestNG suite
     * Input Parameter(s) if any : suite (XmlSuite)
     * Output Parameter(s) if any : none
     */
    static void runSuite(XmlSuite suite) {
        TestNG testng = new TestNG();
        testng.setXmlSuites(Collections.singletonList(suite));
        testng.run();
    }

    /*
     * Method Description : Writes XmlSuite content to a file
     * Input Parameter(s) if any : suite (XmlSuite), filePath (String)
     * Output Parameter(s) if any : none
     */
    public static void writeSuiteToXmlFile(XmlSuite suite, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(suite.toXml());
        } catch (IOException e) {
            logger.error("Failed to write XML file: {}", e.getMessage());
        }
    }

}
