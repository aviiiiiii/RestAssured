package com.example.api.utils;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;


public class TestNGXMLGenerator2 {

    public static void main(String[] args) {
        try {
            String appName = ConfigReader.getProperty("appName");
            String excelPath = "src/test/resources/InputForAPIRuns/"+ConfigReader.getProperty("excelName");

            FileInputStream fis = new FileInputStream(excelPath);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            boolean parallelFlag = Boolean.parseBoolean(ConfigReader.getProperty("parallelFlag"));
            String threadCount = ConfigReader.getProperty("threadCount");

            String configuredTag = ConfigReader.getProperty("tag");

            Map<String, String> suiteParams = new LinkedHashMap<>();
            Properties props = ConfigReader.getAllProperties(); // <-- You’ll add this helper in ConfigReader
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("suiteParam.")) {
                    String paramName = key.substring("suiteParam.".length());
                    String paramValue = props.getProperty(key, "").trim();
                    if (!paramValue.isEmpty()) { // only add non-empty ones
                        suiteParams.put(paramName, paramValue);
                    }
                }
            }

            // TestNG XML header
            StringBuilder xmlBuilder = new StringBuilder();
            xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xmlBuilder.append("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\" >\n");

            // Collect suites
            Map<String, Map<String, List<TestCaseData>>> suiteMap = new LinkedHashMap<>();

            int rowCount = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rowCount; i++) { // Skip header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String testCaseID = getCellValue(row, 0);
                String suiteName = getCellValue(row, 1);
                String testName = getCellValue(row, 2);
                String tags = getCellValue(row, 3);
                String methodName = getCellValue(row, 4);
                String className = getCellValue(row, 5);
                String execute = getCellValue(row, 6);
                String paramName = getCellValue(row, 7);
                String paramValue = getCellValue(row, 8);
                String dependsOn = getCellValue(row, 9);

                if (!"Y".equalsIgnoreCase(execute)) continue;

                if (configuredTag != null && !configuredTag.isEmpty()) {
                    boolean matched = false;
                    if (tags != null && !tags.isEmpty()) {
                        String[] tag = tags.split(",");
                        for (String t : tag) {
                            if (configuredTag.equalsIgnoreCase(t.trim())) {
                                matched = true;
                                break;
                            }
                        }
                    }
                    if (!matched)   continue;
                }

                suiteMap
                        .computeIfAbsent(suiteName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(testName, k -> new ArrayList<>())
                        .add(new TestCaseData(className, methodName, testCaseID, paramName, paramValue, dependsOn, tags));
            }

            // Build XML dynamically
            for (String suiteName : suiteMap.keySet()) {
                xmlBuilder.append("<suite name=\"").append(suiteName).append("\"");

                //parallelFlag and threadCount
                if (parallelFlag) {
                    xmlBuilder.append(" parallel=\"methods\"");
                    xmlBuilder.append(" thread-count=\"").append(threadCount).append("\"");
                }
                xmlBuilder.append(">\n");

                // Add suite-level <parameter> tags
                for (Map.Entry<String, String> entry : suiteParams.entrySet()) {
                    xmlBuilder.append("  <parameter name=\"")
                            .append(entry.getKey())
                            .append("\" value=\"")
                            .append(entry.getValue())
                            .append("\"/>\n");
                }

                Map<String, List<TestCaseData>> tests = suiteMap.get(suiteName);
                for (String testName : tests.keySet()) {
                    xmlBuilder.append("  <test name=\"").append(testName).append("\">\n");
                    xmlBuilder.append("    <classes>\n");

                    Map<String, List<TestCaseData>> classMap = new LinkedHashMap<>();

                    for (TestCaseData data : tests.get(testName)) {
                        classMap.computeIfAbsent(data.className, k -> new ArrayList<>()).add(data);
                    }

                    for (String className : classMap.keySet()) {
                        xmlBuilder.append("      <class name=\"").append(className).append("\">\n");
                        xmlBuilder.append("        <methods>\n");
                        for (TestCaseData method : classMap.get(className)) {
                            xmlBuilder.append("          <include name=\"").append(method.methodName).append("\"/>\n");
                        }
                        xmlBuilder.append("        </methods>\n");
                        xmlBuilder.append("      </class>\n");
                    }

                    xmlBuilder.append("    </classes>\n");
                    xmlBuilder.append("  </test>\n");
                }

                xmlBuilder.append("</suite>\n");
            }

            workbook.close();

            // Write to file
            String xmlFilePath = ConfigReader.getProperty("generatedXMLName");
            try (FileWriter writer = new FileWriter(xmlFilePath)) {
                writer.write(xmlBuilder.toString());
            }

            System.out.println("TestNG XML generated successfully: " + xmlFilePath);
//            runGeneratedXML(xmlFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate TestNG XML: " + e.getMessage());
        }
    }

    private static String getCellValue(Row row, int cellIndex) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell == null) return "";
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private static void runGeneratedXML(String xmlFilePath) {
        try {
            TestNG testng = new TestNG();
            List<String> suites = new ArrayList<>();
            suites.add(xmlFilePath);
            testng.setTestSuites(suites);
            System.out.println("Running TestNG suite from: " + xmlFilePath);
            testng.run();
            System.out.println("TestNG execution completed!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run generated TestNG XML: " + e.getMessage());
        }
    }


    // Helper class for test case data
    private static class TestCaseData {
        String className;
        String methodName;
        String testCaseID;
        String paramName;
        String paramValue;
        String dependsOn;
        String tags;

        public TestCaseData(String className, String methodName, String testCaseID,
                            String paramName, String paramValue, String dependsOn, String tags) {
            this.className = className;
            this.methodName = methodName;
            this.testCaseID = testCaseID;
            this.paramName = paramName;
            this.paramValue = paramValue;
            this.dependsOn = dependsOn;
            this.tags = tags;
        }
    }
}

