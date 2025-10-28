package com.example.api.utils;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class TestNGXMLGenerator2 {

    public static void main(String[] args) {
        try {
            String appName = ConfigReader.getProperty("appName");
            String excelPath = ConfigReader.getProperty("testNGPath");

            FileInputStream fis = new FileInputStream(excelPath);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            boolean parallelFlag = Boolean.parseBoolean(ConfigReader.getProperty("parallelFlag"));
            String threadCount = ConfigReader.getProperty("threadCount");

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

                String suiteName = getCellValue(row, 0);
                String testName = getCellValue(row, 1);
                String className = getCellValue(row, 2);
                String testCaseID = getCellValue(row, 3);
                String methodName = getCellValue(row, 4);
                String paramName = getCellValue(row, 5);
                String paramValue = getCellValue(row, 6);
                String execute = getCellValue(row, 7);
                String dependsOn = getCellValue(row, 8);

                if (!"Y".equalsIgnoreCase(execute)) continue;

                suiteMap
                        .computeIfAbsent(suiteName, k -> new LinkedHashMap<>())
                        .computeIfAbsent(testName, k -> new ArrayList<>())
                        .add(new TestCaseData(className, methodName, testCaseID, paramName, paramValue, dependsOn));
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
            String xmlFilePath = "testng_2.xml";
            try (FileWriter writer = new FileWriter(xmlFilePath)) {
                writer.write(xmlBuilder.toString());
            }

            System.out.println("✅ TestNG XML generated successfully: " + xmlFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Failed to generate TestNG XML: " + e.getMessage());
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

    // Helper class for test case data
    private static class TestCaseData {
        String className;
        String methodName;
        String testCaseID;
        String paramName;
        String paramValue;
        String dependsOn;

        public TestCaseData(String className, String methodName, String testCaseID,
                            String paramName, String paramValue, String dependsOn) {
            this.className = className;
            this.methodName = methodName;
            this.testCaseID = testCaseID;
            this.paramName = paramName;
            this.paramValue = paramValue;
            this.dependsOn = dependsOn;
        }
    }
}

