package com.dawgs.marketplace;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarketplaceApplication {

    public static void main(String[] args) {
        // Load .env from project root (one level up from backend/) for local dev.
        // On Railway, real environment variables are used and .env is ignored.
        Dotenv dotenv = Dotenv.configure()
                .directory("../")
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        SpringApplication.run(MarketplaceApplication.class, args);
    }
}
