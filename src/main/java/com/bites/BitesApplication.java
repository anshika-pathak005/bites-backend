package com.bites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * The main entry point for the Bites restaurant ordering backend.
 * Starting this class boots up the whole Spring Boot application
 * (web server, database connections, security, etc.).
 */
@SpringBootApplication
public class BitesApplication {

    // Starts the Spring Boot application. This is the first code that runs
    // when the backend launches.
    public static void main(String[] args) {
        SpringApplication.run(BitesApplication.class, args);
    }
}
