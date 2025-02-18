package com.example.speech.aiservice.vn.controller.http.restful;

import com.example.speech.aiservice.vn.dto.FullProcessResponseDTO;
import com.example.speech.aiservice.vn.dto.TextToSpeechResponseDTO;
import com.example.speech.aiservice.vn.dto.WebCrawlResponseDTO;
import com.example.speech.aiservice.vn.service.crawl.WebCrawlerService;
import com.example.speech.aiservice.vn.service.google.GoogleChromeLauncherService;
import com.example.speech.aiservice.vn.service.selenium.WebDriverLauncherService;
import com.example.speech.aiservice.vn.service.speech.SpeechService;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
public class TotalProcessController {

    private final WebCrawlerService webCrawlerService;
    private final SpeechService speechService;
    private final WebDriverLauncherService webDriverLauncherService;
    private final GoogleChromeLauncherService googleChromeLauncherService;

    // Constructor Injection
    @Autowired
    public TotalProcessController(WebCrawlerService webCrawlerService, SpeechService speechService, WebDriverLauncherService webDriverLauncherService, GoogleChromeLauncherService googleChromeLauncherService) {
        this.webCrawlerService = webCrawlerService;
        this.speechService = speechService;
        this.webDriverLauncherService = webDriverLauncherService;
        this.googleChromeLauncherService = googleChromeLauncherService;
    }


    @GetMapping("/full")
    public ResponseEntity<FullProcessResponseDTO> startFullProcess(@RequestParam String crawlUrl, @RequestParam String speechToTextUrl) {
        WebDriver chromeDriver = null;
        try {
            googleChromeLauncherService.openGoogleChrome();
            chromeDriver = webDriverLauncherService.initWebDriver();

            // Crawl data on Chivi.App website
            WebCrawlResponseDTO webCrawlResponseDTO = webCrawlerService.webCrawlResponseDTO(chromeDriver, crawlUrl);

            // Convert text to speech with ADMICRO | Vietnamese Speech Synthesis
            TextToSpeechResponseDTO textToSpeechResponseDTO = speechService.textToSpeechResponseDTO(chromeDriver, speechToTextUrl, webCrawlResponseDTO.getFilePath());

            // Aggregated DTO response
            FullProcessResponseDTO fullProcessResponse = new FullProcessResponseDTO(webCrawlResponseDTO, textToSpeechResponseDTO);
            return ResponseEntity.ok(fullProcessResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            if (chromeDriver != null) {
                webDriverLauncherService.shutDown();
            }
            googleChromeLauncherService.shutdown();
        }
    }
}
