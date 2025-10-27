

package com.example.api.utils;

import org.testng.annotations.DataProvider;
import java.util.List;
import java.util.Map;

public class ExcelUtilsUsersAPI {

    @DataProvider(name = "registerData")
    public Object[][] getRegisterData() throws Exception {
        return getDataFromSheet("UserRegister");
    }

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() throws Exception {
        return getDataFromSheet("Login");
    }

    private Object[][] getDataFromSheet(String sheetName) throws Exception {
        String filePath = "src/test/resources/API_Users.xlsx";

        List<Map<String, String>> excelData = ExcelUtils.readExcelData(filePath, sheetName);

        Object[][] data = new Object[excelData.size()][1];

        for (int i = 0; i < excelData.size(); i++) {
            data[i][0] = excelData.get(i);
        }

        return data;
    }

}
