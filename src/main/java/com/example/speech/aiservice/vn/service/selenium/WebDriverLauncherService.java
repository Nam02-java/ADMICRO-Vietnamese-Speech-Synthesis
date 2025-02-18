package com.example.speech.aiservice.vn.service.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class WebDriverLauncherService  {

    private WebDriver driver;

    public WebDriver initWebDriver() {

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");

        driver = new ChromeDriver(options);

        return driver;
    }


    public void shutDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
