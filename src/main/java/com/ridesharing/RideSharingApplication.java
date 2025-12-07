package com.ridesharing;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RideSharingApplication {

    public static void main(String[] args) {

        // Load .env from project root (Git safe)
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir")) // root directory
                .ignoreIfMissing()
                .load();

        // Set environment variables as system properties
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(RideSharingApplication.class, args);
    }
}
