package com.example.speech.aiservice.vn;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

/**
 * https://googlechromelabs.github.io/chrome-for-testing/
 * taskkill /F /IM chrome.exe
 */
