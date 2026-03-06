package com.dawgs.marketplace;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarketplaceApplication {

    public static void main(String[] args) {
        // Load .env from current dir or parent dir (handles different working directories).
        // On Railway, real env vars are used and .env is absent — ignoreIfMissing handles that.
        Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
        if (dotenv.get("DB_URL") == null) {
            dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
        }

        // Map env vars to Spring property names
        setIfPresent(dotenv, "DB_URL", "spring.datasource.url");
        setIfPresent(dotenv, "DB_USERNAME", "spring.datasource.username");
        setIfPresent(dotenv, "DB_PASSWORD", "spring.datasource.password");
        setIfPresent(dotenv, "GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_ID");
        setIfPresent(dotenv, "JWT_SECRET", "JWT_SECRET");

        SpringApplication.run(MarketplaceApplication.class, args);
    }

    private static void setIfPresent(Dotenv dotenv, String envKey, String propertyName) {
        String value = dotenv.get(envKey);
        if (value != null) {
            System.setProperty(propertyName, value);
        }
    }
}
