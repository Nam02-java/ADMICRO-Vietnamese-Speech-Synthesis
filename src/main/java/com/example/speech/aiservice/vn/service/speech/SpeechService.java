package com.example.speech.aiservice.vn.service.speech;

import com.example.speech.aiservice.vn.dto.TextToSpeechResponseDTO;
import com.example.speech.aiservice.vn.service.filehandler.FileNameService;
import com.example.speech.aiservice.vn.service.filehandler.FileReaderService;
import com.example.speech.aiservice.vn.service.google.GoogleAudioDownloaderService;
import com.example.speech.aiservice.vn.service.wait.WaitService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SpeechService {
    private final FileNameService fileNameService;
    private final FileReaderService fileReaderService;
    private final WaitService waitService;
    private final GoogleAudioDownloaderService googleAudioDownloaderService;
    private final String directoryPath = "E:\\CongViecHocTap\\Voice\\";
    private final String baseFileName = "voice";
    private final String fileExtension = ".mp4";

    @Autowired
    public SpeechService(FileNameService fileNameService, FileReaderService fileReaderService, WaitService waitService, GoogleAudioDownloaderService googleAudioDownloaderService) {
        this.fileNameService = fileNameService;
        this.fileReaderService = fileReaderService;
        this.waitService = waitService;
        this.googleAudioDownloaderService = googleAudioDownloaderService;
    }

    public TextToSpeechResponseDTO textToSpeechResponseDTO(WebDriver driver, String url, String contentfilePath) throws InterruptedException, IOException {

        driver.get(url);

        waitService.waitForSeconds(5);

        String content = fileReaderService.readFileContent(contentfilePath);

        WebElement textArea = driver.findElement(By.id("edit-content"));
        textArea.sendKeys(content);

        waitService.waitForSeconds(5);

        driver.findElement(By.id("submit_btn")).click();
        waitService.waitForSeconds(10);

        String audioUrl = driver.findElement(By.id("audio")).getAttribute("src");
        System.out.println("Audio URL: " + audioUrl);

        String fileName = fileNameService.getAvailableFileName(directoryPath, baseFileName, fileExtension);

        googleAudioDownloaderService.download(audioUrl, directoryPath + fileName);

        String audioFilePath = directoryPath + fileName;

        return new TextToSpeechResponseDTO("Successful conversion", url, audioUrl, audioFilePath);
    }
}
