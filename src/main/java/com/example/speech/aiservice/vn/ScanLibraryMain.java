package com.example.speech.aiservice.vn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.speech.aiservice.vn"})
public class ScanLibraryMain {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ScanLibraryMain.class);
        app.setAdditionalProfiles("library-scan");  // Configure settings in code
        app.run(args);
    }
}
