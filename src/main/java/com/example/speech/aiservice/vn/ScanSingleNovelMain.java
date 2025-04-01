package com.example.speech.aiservice.vn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.speech.aiservice.vn"})
public class ScanSingleNovelMain {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ScanSingleNovelMain.class);
        app.setAdditionalProfiles("single-novel"); // Configure settings in code
        app.run(args);
    }
}
