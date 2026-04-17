package org.example.isdcmproject.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RestConfig {

    public static final String BASE_URL;

    static {
        Properties props = new Properties();
        try (InputStream is = RestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            // fallback al valor per defecte
        }
        BASE_URL = props.getProperty("rest.base.url", "http://localhost:8080/entrega-2-1.0-SNAPSHOT/api");
    }

    private RestConfig() {}
}
