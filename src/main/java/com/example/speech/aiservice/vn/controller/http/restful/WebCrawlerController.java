package com.example.speech.aiservice.vn.controller.http.restful;

import com.example.speech.aiservice.vn.dto.response.WebCrawlResponseDTO;
import com.example.speech.aiservice.vn.model.entity.Chapter;
import com.example.speech.aiservice.vn.service.crawl.WebCrawlerService;
import com.example.speech.aiservice.vn.service.google.GoogleChromeLauncherService;
import com.example.speech.aiservice.vn.service.selenium.WebDriverLauncherService;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawl")
public class WebCrawlerController {
    private final WebCrawlerService webCrawlerService;
    private final WebDriverLauncherService webDriverLauncherService;
    private final GoogleChromeLauncherService googleChromeLauncherService;


    // Constructor Injection
    @Autowired
    public WebCrawlerController(
            WebCrawlerService webCrawlerService,
            WebDriverLauncherService webDriverLauncherService,
            GoogleChromeLauncherService googleChromeLauncherService) {
        this.webCrawlerService = webCrawlerService;
        this.webDriverLauncherService = webDriverLauncherService;
        this.googleChromeLauncherService = googleChromeLauncherService;
    }

    @GetMapping("/text")
    public ResponseEntity<WebCrawlResponseDTO> crawlText(@RequestParam String url) {
        //set here for not warning
        Chapter chapter = null;

        WebDriver chromeDriver = null;
        try {
            //googleChromeLauncherService.openGoogleChrome();
            //    chromeDriver = webDriverLauncherService.initWebDriver();

            WebCrawlResponseDTO response = webCrawlerService.webCrawlResponseDTO(chromeDriver, null, chapter);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WebCrawlResponseDTO("Error", null, null));

        } finally {
            if (chromeDriver != null) {
                webDriverLauncherService.shutDown(chromeDriver);
            }
            googleChromeLauncherService.shutdown();
        }
    }
}
