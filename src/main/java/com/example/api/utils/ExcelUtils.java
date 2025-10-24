package com.example.api.utils;

import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ExcelUtils {
    public static List<Map<String, String>> readExcelData(String filePath, String sheetName) throws Exception {
        List<Map<String, String>> dataList = new ArrayList<>();
        FileInputStream fis = new FileInputStream(new File(filePath));
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheet(sheetName);

        Row headerRow = sheet.getRow(0);
        int totalCols = headerRow.getPhysicalNumberOfCells();

        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row currentRow = sheet.getRow(i);
            Map<String, String> dataMap = new HashMap<>();
            for (int j = 0; j < totalCols; j++) {
                String key = headerRow.getCell(j).getStringCellValue();
                Cell cell = currentRow.getCell(j);
                String value = (cell == null) ? "" : cell.toString();
                dataMap.put(key, value);
            }
            dataList.add(dataMap);
        }
        workbook.close();
        fis.close();
        return dataList;
    }
}
