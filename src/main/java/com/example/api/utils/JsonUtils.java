package com.example.api.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;
import java.io.File;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    @DataProvider(name = "registerData")
    public Object[][] getRegisterData() throws Exception {
        return getDataFromJson("src/test/resources/json TCs/UserRegister.json");
    }

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() throws Exception {
        return getDataFromJson("src/test/resources/json TCs/Login.json");
    }

    @DataProvider(name = "changePasswordData")
    public Object[][] getChangePasswordData() throws Exception {
        return getDataFromJson("src/test/resources/json TCs/ChangePassword.json");
    }

    private Object[][] getDataFromJson(String filePath) throws Exception {
        // JSON reading logic moved here
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> jsonData =
                mapper.readValue(new File(filePath), new TypeReference<List<Map<String, String>>>() {});

        Object[][] data = new Object[jsonData.size()][1];
        for (int i = 0; i < jsonData.size(); i++) {
            data[i][0] = jsonData.get(i);
        }
        return data;
    }
}
