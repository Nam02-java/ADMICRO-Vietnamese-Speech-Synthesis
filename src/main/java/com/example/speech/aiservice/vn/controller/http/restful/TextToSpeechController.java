package com.example.speech.aiservice.vn.controller.http.restful;

import com.example.speech.aiservice.vn.dto.request.TextToSpeechRequestDTO;
import com.example.speech.aiservice.vn.dto.response.TextToSpeechResponseDTO;
import com.example.speech.aiservice.vn.model.entity.Chapter;
import com.example.speech.aiservice.vn.service.google.GoogleChromeLauncherService;
import com.example.speech.aiservice.vn.service.selenium.WebDriverLauncherService;
import com.example.speech.aiservice.vn.service.speech.SpeechService;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/text-to-speech")
public class TextToSpeechController {

    private final SpeechService speechService;
    private final WebDriverLauncherService webDriverLauncherService;
    private final GoogleChromeLauncherService googleChromeLauncherService;


    // Constructor Injection
    @Autowired
    public TextToSpeechController(
            SpeechService speechService,
            WebDriverLauncherService webDriverLauncherService,
            GoogleChromeLauncherService googleChromeLauncherService) {
        this.speechService = speechService;
        this.webDriverLauncherService = webDriverLauncherService;
        this.googleChromeLauncherService = googleChromeLauncherService;
    }


    @PostMapping("/convert")
    public ResponseEntity<TextToSpeechResponseDTO> convertTextToSpeech(@RequestBody TextToSpeechRequestDTO textToSpeechRequestDTO) throws InterruptedException {
        WebDriver chromeDriver = null;


        //set here for not warning
        Chapter chapter = null;

        try {
            //   googleChromeLauncherService.openGoogleChrome();
            // chromeDriver = webDriverLauncherService.initWebDriver();

            TextToSpeechResponseDTO response = speechService.textToSpeechResponseDTO(chromeDriver, textToSpeechRequestDTO.getTextToSpeechUrl(), textToSpeechRequestDTO.getContentPath(), null, chapter);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TextToSpeechResponseDTO("Error", null, null, null));
        } finally {
            if (chromeDriver != null) {
                webDriverLauncherService.shutDown(chromeDriver);
            }
            googleChromeLauncherService.shutdown();
        }
    }
}
