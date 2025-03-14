package com.example.speech.aiservice.vn.controller.http.restful;

import com.example.speech.aiservice.vn.dto.request.FullProcessRequestDTO;
import com.example.speech.aiservice.vn.dto.response.*;
import com.example.speech.aiservice.vn.model.entity.Chapter;
import com.example.speech.aiservice.vn.service.crawl.WebCrawlerService;
import com.example.speech.aiservice.vn.service.google.GoogleChromeLauncherService;
import com.example.speech.aiservice.vn.service.selenium.WebDriverLauncherService;
import com.example.speech.aiservice.vn.service.speech.SpeechService;
import com.example.speech.aiservice.vn.service.video.VideoCreationService;
import com.example.speech.aiservice.vn.service.youtube.YoutubeUploadService;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/process")
public class TotalProcessController {

    private final WebCrawlerService webCrawlerService;
    private final SpeechService speechService;
    private final VideoCreationService videoCreationService;
    private final YoutubeUploadService youtubeUploadService;
    private final WebDriverLauncherService webDriverLauncherService;
    private final GoogleChromeLauncherService googleChromeLauncherService;

    // Constructor Injection
    @Autowired
    public TotalProcessController(WebCrawlerService webCrawlerService, SpeechService speechService, VideoCreationService videoCreationService, YoutubeUploadService youtubeUploadService, WebDriverLauncherService webDriverLauncherService, GoogleChromeLauncherService googleChromeLauncherService) {
        this.webCrawlerService = webCrawlerService;
        this.speechService = speechService;
        this.videoCreationService = videoCreationService;
        this.youtubeUploadService = youtubeUploadService;
        this.webDriverLauncherService = webDriverLauncherService;
        this.googleChromeLauncherService = googleChromeLauncherService;
    }


    @PostMapping("/full-workflow")
    public ResponseEntity<FullProcessResponseDTO> startFullProcess(@RequestBody FullProcessRequestDTO request) {
        WebDriver chromeDriver = null;
        try {

            //set here for not warning
            Chapter chapter = null;

            // googleChromeLauncherService.openGoogleChrome();
            //chromeDriver = webDriverLauncherService.initWebDriver();

            // Crawl data on Chivi.App website
            WebCrawlResponseDTO webCrawlResponseDTO = webCrawlerService.webCrawlResponseDTO(chromeDriver, null, chapter);

            // Convert text to speech with ADMICRO | Vietnamese Speech Synthesis
            TextToSpeechResponseDTO textToSpeechResponseDTO = speechService.textToSpeechResponseDTO(chromeDriver, request.getTextToSpeechUrl(), "test", null, chapter);

            // Create videos using mp4 files combined with photos
            String imagePath = "E:\\CongViecHocTap\\Picture\\picture.png";
            CreateVideoResponseDTO createVideoResponseDTO = videoCreationService.createVideoResponseDTO(textToSpeechResponseDTO.getFilePath(), imagePath,  null, null);

            // Upload video to youtube with youtube data API
            YoutubeUploadResponseDTO youtubeUploadResponseDTO = youtubeUploadService.upload(createVideoResponseDTO.getCreatedVideoFilePath(), null, chapter);

            // Aggregated DTO response
            FullProcessResponseDTO fullProcessResponse = new FullProcessResponseDTO(webCrawlResponseDTO, textToSpeechResponseDTO, createVideoResponseDTO, youtubeUploadResponseDTO);

            System.out.println(fullProcessResponse);
            return ResponseEntity.ok(fullProcessResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            if (chromeDriver != null) {
                webDriverLauncherService.shutDown(chromeDriver);
            }
            googleChromeLauncherService.shutdown();
        }
    }
}
