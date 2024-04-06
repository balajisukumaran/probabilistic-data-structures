package org.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private Properties configProps;
    InputStream input = null;

    public ConfigLoader() {
        try {
            configProps = new Properties();

            String propPath = System.getProperty("properties.path");
            input = new FileInputStream(propPath);
            configProps.load(input);

        } catch (Exception e) {
            System.out.println("Config File Error");
        }
    }

    public String getProperty(String key) {
        return configProps.getProperty(key);
    }
}
