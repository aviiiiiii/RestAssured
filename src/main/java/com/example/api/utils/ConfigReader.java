package com.example.api.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties = new Properties();

    static {
        try {
          FileInputStream fileInput = new FileInputStream("src/test/resources/config.properties");
            properties.load(fileInput);
        }catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load the config.properties file");
        }

    }

    public static String getProperty (String key) {
        return properties.getProperty(key);
    }
}
